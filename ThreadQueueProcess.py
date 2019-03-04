from threading import Thread
import time
import smtplib
import os
import zipfile
import tarfile
import random
import subprocess
import shutil
import hashlib
from subprocess import STDOUT, PIPE
from email.message import EmailMessage


JAVA_OUT_COMPILED_CLASSES = 'java_img_processing\\bin'
JAVA_SOURCEPATH_CLASSES = 'java_img_processing\\src\\java_classes'
JAVA_LIBS_PATH = 'java_img_processing\\lib'
JAVA_FILES_PATH = 'java_img_processing\\src\\java_classes'
JAVA_PACKAGE_NAME = 'java_classes'
JAVA_MAIN_CLASS = 'Process_image'

JAVA_OPTIONS = 'Picked up _JAVA_OPTIONS: -Xmx4096m'
JAVA_OUTPUT_DETECT_CLASS = "b'Finish WATERDETECT"
JAVA_OUTPUT_PREDICT_CLASS = 'Finish CREATEFLOOD'

SERVER_MAIL_ADDRESS = 'smtp.gmail.com'
SERVER_MAIL_PORT = 587
MAIL_SERVER = 'floodgenerator@gmail.com'
MAIL_PASS = 'xxaumwtdntsucxkz'

queue = None
lock = None
manageDB_results = None


class QueueSolveThread(Thread):
    """
        Thread for processing the queue requests
    """
    def __init__(self, queue, lock, manageDB_results):
        """
            Constructor for Thread object

            :param queue - in which the requests are placed
        """
        Thread.__init__(self)
        self.queue = queue
        self.lock = lock
        self.manageDB_results = manageDB_results

    # def send_mail_request_complet(self, )

    def unzip_file(self, path):
        """
            extract zip tar or tar.gz path

            :param path: path to file which will be unarchived
            :return path where the file was unziped
                    -1 if file path is not a zip tar or tar.gz
        """
        directory_to_extract = path.split('\\')[-1].split('.')[0]
        path_where_directory = path[0: path.rfind('\\')]
        final_name = os.path.join(path_where_directory, directory_to_extract)

        # create directory where to unzip
        if not os.path.isdir(final_name):
            os.mkdir(final_name)
        else:
            random_nr = random.randint(0, 10000)
            final_name += str(random_nr)
            os.mkdir(final_name)

        # check archive type
        if path.endswith('.zip'):
            opener, mode = zipfile.ZipFile, 'r'
        elif path.endswith('.tar.gz'):
            opener, mode = tarfile.open, 'r:gz'
        elif path.endswith('.tar'):
            opener, mode = tarfile.open, 'r:'

        file = None
        try:
            file = opener(path, mode)
        except:
            return -1
        with file:
            try:
                file.extractall(final_name)
            except:
                return -1
        return os.path.join(final_name, directory_to_extract)

    def check_unziped_folder(self, path):
        """
            test if path contains a multiple if 3 files nif, green and topo

            :return 0 - good
                    -1 - not a processable folder
        """
        list_files = os.listdir(path)

        if len(list_files) % 3 != 0:
            return -1

        for i in range(len(list_files) // 3):
            file_nif = str(i) + '_nif.tif'
            file_green = str(i) + '_green.tif'
            file_topo = str(i) + '_topo.tif'

            if file_nif not in list_files or file_green not in list_files or file_topo not in list_files:
                shutil.rmtree(path[0: path.rfind('\\')], ignore_errors = True)
                return -1

        return 0


    def run(self):
        """
            Check the queue for requests
            If there are any each request is unziped and processed
            After the request is processed an email is send
        """
        compile_java_files()
        while True:
            item = self.queue.get()
            boolean_processed_success = True
            boolean_ok_to_mail = True

            result_unzip_paht = self.unzip_file(item['file'])
            if result_unzip_paht != -1:
                result_check_zip = self.check_unziped_folder(result_unzip_paht)
                if result_check_zip != -1:
                    os.remove(item['file'])

                    # create direcrory to save the results
                    result_directory = os.path.join(result_unzip_paht, 'result')
                    os.mkdir(result_directory)

                    print('aici')

                    if process_files(result_unzip_paht, result_directory) == 1:
                        boolean_processed_success = False
                    print(boolean_processed_success)
                else:
                    boolean_processed_success = False
            else:
                boolean_processed_success = False

            # if one of the process fails delete archive and proccesed junk
            if boolean_processed_success == False:
                try:
                    os.remove(item['file'])
                except OSError:
                    pass
                boolean_ok_to_mail = False

            zip_file_paht_hash = None
            if boolean_ok_to_mail:
                path_result = os.path.join(result_unzip_paht, 'result')
                zip_name = os.path.join(result_unzip_paht, 'results')
                zip_file_paht = shutil.make_archive(zip_name, 'zip', path_result)
                zip_file_paht_hash = hashlib.md5(zip_file_paht.encode())
                zip_file_paht_hash = zip_file_paht_hash.hexdigest()

                try:
                    self.lock.acquire()

                    pack = {}
                    pack['result_path'] = zip_file_paht
                    pack['result_hash'] = zip_file_paht_hash
                    self.manageDB_results.add_pack(pack)

                finally:
                    self.lock.release()

            send_mail(boolean_ok_to_mail, item['email'], zip_file_paht_hash)


def compile_java_files():
    """
        compile all java files that are used for image processing
        and link all libs
    """
    java_files = JAVA_FILES_PATH + '\\*java'
    java_libs = JAVA_LIBS_PATH + '\\*'
    subprocess.Popen(['javac', '-d', JAVA_OUT_COMPILED_CLASSES,
                     '-sourcepath', JAVA_SOURCEPATH_CLASSES,
                     '-cp', java_libs, java_files], shell=True, stdout=True)


def process_files(path_files, result_directory):
    """
        run a java process that will solve the request
        param : path_files - path to the folder where images have been unziped
        param : result_directory
        return : 1 - if the process have failed
                 0 - if the process had succed
    """
    java_libs = JAVA_LIBS_PATH + '\\*'
    compiled_libs_and_classes = JAVA_OUT_COMPILED_CLASSES + ';' + java_libs
    java_main_class = JAVA_PACKAGE_NAME + '.' + JAVA_MAIN_CLASS
    cmd = ['java', '-cp', compiled_libs_and_classes,
            java_main_class, path_files, result_directory]
    stdin = PIPE
    stdout = PIPE
    stderr = STDOUT
    proc = subprocess.Popen(cmd, stdin=PIPE, stdout=PIPE, stderr=STDOUT, shell=True)
    stdout, stderr = proc.communicate()
    output = str(stdout).replace('\\r','').split('\\n')
    final_output = str(output[0]) + str(output[1]) + str(output[2])
    correct_output = JAVA_OUTPUT_DETECT_CLASS + JAVA_OUTPUT_PREDICT_CLASS + JAVA_OPTIONS

    if (final_output == correct_output):
        return 0
    return 1


def send_mail(result_process_images, send_mail_to_address, path):
    """
        send a mail with the link to a html, from which the user can download
        resulted images from request

        send a mail with fail message if the processing if the images fails

        :param result_process_images - result of the image processing 1;0

        :param sent_mail_to_address - the mail address to which the mail
                                      will be send

        :param path - to the file that will be downloaded
    """
    mail = smtplib.SMTP(SERVER_MAIL_ADDRESS, SERVER_MAIL_PORT)
    mail.ehlo()
    mail.starttls()
    mail.login(MAIL_SERVER, MAIL_PASS)

    succed_mail_link = 'http://127.0.0.1:5000/download?file_hash=' + str(path)

    succed_mail_content = ('Subject: Flood images processed results\n\n\
    Your images have been processed\n\
    Click the following link to download the content\n    ' + succed_mail_link)

    fail_mail_content = ('Subject: Flood images processed results\n\n\
    Fail to process your images\n\
    Possible causes:\n\
        - incorrect file naming\n\
        - missing files')

    print('mail')
    try:
        if(result_process_images):
            mail.sendmail(MAIL_SERVER, send_mail_to_address, succed_mail_content)
        else:
            mail.sendmail(MAIL_SERVER, send_mail_to_address, fail_mail_content)
    finally:
        mail.quit()








