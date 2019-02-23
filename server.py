import os
from flask import Flask, flash, request, redirect, url_for, render_template
from flask import send_from_directory
from werkzeug.utils import secure_filename
from queue import Queue
import ThreadQueueProcess


APP_ROOT = os.path.dirname(os.path.abspath(__file__))
ALLOWED_EXTENSIONS = set(['zip', 'rar', 'tar.gz', 'tar.bz2', 'tgz'])


queue = Queue(0)

app = Flask(__name__)
upload_path = os.path.join(APP_ROOT, 'uploads/')


def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS


@app.route('/uploader', methods = ['GET', 'POST'])
def index_route():
    target = os.path.join(APP_ROOT, 'uploads/')

    if not os.path.isdir(target):
        os.mkdir(target)

    email = request.form['email']

    for file in request.files.getlist("file"):
        filename = file.filename
        if(allowed_file(filename)):
            destination = "/".join([target, filename])
            file.save(destination)
        else:
            return render_template("fail.html")

    queue_item = {}
    queue_item['email'] = email
    queue_item['file'] = "uploads\\" + str(filename)
    queue.put(queue_item)

    return render_template("complete.html", email_to_send = email)


@app.route('/')
def template_file():
    return render_template('upload.html')

@app.route('/down')
def down():
    return render_template("fail.html")


if __name__ == '__main__':
    # run multithreaded flask server
    threadQueue = ThreadQueueProcess.QueueSolveThread(queue)
    threadQueue.start()
    app.run(threaded=True, debug=True)
