import os
import threading
import shutil
import time
from queue import Queue
from flask import send_file, Flask, request, render_template
from db_results import manageDB as manageDb_results
import ThreadQueueProcess


APP_ROOT = os.path.dirname(os.path.abspath(__file__))
ALLOWED_EXTENSIONS = set(['zip', 'rar', 'tar.gz', 'tar.bz2', 'tgz'])

LOCK = threading.Lock()

# start data base for users
manageDb_results.initialize()
manageDb_results.create_tables()


queue = Queue(0)

app = Flask(__name__)
upload_path = os.path.join(APP_ROOT, 'uploads/')


def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS


@app.route('/uploader', methods=['GET', 'POST'])
def index_route():
    target = os.path.join(APP_ROOT, 'uploads/')

    if not os.path.isdir(target):
        os.mkdir(target)

    email = request.form['email']

    for file in request.files.getlist("file"):
        filename = file.filename
        if allowed_file(filename):
            destination = "/".join([target, filename])
            file.save(destination)
        else:
            return render_template("fail.html")

    queue_item = {}
    queue_item['email'] = email
    queue_item['file'] = "uploads\\" + str(filename)
    queue.put(queue_item)

    return render_template("complete.html", email_to_send=email)


@app.route('/')
def template_file():
    return render_template('upload.html')

@app.route('/download')
def down():
    """
        Download a pack processed by the server based on a hash
        After the file is downloaded a daemon thread is startded
        that will delete the processed files correspondint to that hash
    """
    recived_args = request.args
    request_pack = None
    if 'file_hash' in recived_args:
        try:
            LOCK.acquire()
            request_pack = manageDb_results.get_pack(str(recived_args['file_hash']))
        finally:
            LOCK.release()

    if request_pack != None and len(request_pack) > 0:
        pack = request_pack[0]
        path = pack.get('result_path')
        if path != None and os.path.exists(path):
            root_path = path[0: path[0:path.rfind('\\')].rfind('\\')]

            # wait 10 min until the file is deleted
            time_sleep = 10 * 60

            # if the file is bigger that 1 GB wait 1h
            if os.path.getsize(path) > 1200000000:
                time_sleep = 1 * 60 * 60

            thread_del = threading.Thread(target=thread_delete, args=(root_path, time_sleep))
            thread_del.daemon = True
            thread_del.start()

            return send_file(path, as_attachment=True, attachment_filename='result.zip')

    return render_template("file_not_found.html")


def thread_delete(path, time_sleep):
    """
    Function that will spawn a daemon thread which will
    delete a path to a result after the result is downloaded
    param : path - valid path that will be deleted
    param : time_sleep - untile the file path is deleted
    """
    time.sleep(time_sleep)
    shutil.rmtree(path, ignore_errors=True)
    print(path)
    print(os.path.exists(path))


if __name__ == '__main__':
    # run multithreaded flask server
    threadQueue = ThreadQueueProcess.QueueSolveThread(queue, LOCK, manageDb_results)
    threadQueue.start()
    app.run(threaded=True, debug=False)
