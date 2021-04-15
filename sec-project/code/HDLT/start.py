import sys,subprocess,csv,pexpect,time,os


NUMBER_USERS = 10
NUMBER_BYZANTINE_USERS = 3
MAVEN_PATH = "../../maven/bin/mvn"
LOCATION_REPORT_FILE = "files/location_reports"

user_process_array = []
byzantine_user_process_array = []


SERVER_START_COMMAND="java -Dfile.encoding=UTF-8 -classpath target/classes:../../m2/repository/io/grpc/grpc-netty-shaded/1.28.0/grpc-netty-shaded-1.28.0.jar:../../m2/repository/io/grpc/grpc-core/1.28.0/grpc-core-1.28.0.jar:../../m2/repository/com/google/code/gson/gson/2.8.6/gson-2.8.6.jar:../../m2/repository/com/google/android/annotations/4.1.1.4/annotations-4.1.1.4.jar:../../m2/repository/com/google/errorprone/error_prone_annotations/2.3.4/error_prone_annotations-2.3.4.jar:../../m2/repository/io/perfmark/perfmark-api/0.19.0/perfmark-api-0.19.0.jar:../../m2/repository/io/grpc/grpc-protobuf/1.28.0/grpc-protobuf-1.28.0.jar:../../m2/repository/io/grpc/grpc-api/1.28.0/grpc-api-1.28.0.jar:../../m2/repository/io/grpc/grpc-context/1.28.0/grpc-context-1.28.0.jar:../../m2/repository/com/google/code/findbugs/jsr305/3.0.2/jsr305-3.0.2.jar:../../m2/repository/org/codehaus/mojo/animal-sniffer-annotations/1.18/animal-sniffer-annotations-1.18.jar:../../m2/repository/com/google/protobuf/protobuf-java/3.11.0/protobuf-java-3.11.0.jar:../../m2/repository/com/google/guava/guava/28.1-android/guava-28.1-android.jar:../../m2/repository/com/google/guava/failureaccess/1.0.1/failureaccess-1.0.1.jar:../../m2/repository/com/google/guava/listenablefuture/9999.0-empty-to-avoid-conflict-with-guava/listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar:../../m2/repository/org/checkerframework/checker-compat-qual/2.5.5/checker-compat-qual-2.5.5.jar:../../m2/repository/com/google/j2objc/j2objc-annotations/1.3/j2objc-annotations-1.3.jar:../../m2/repository/com/google/api/grpc/proto-google-common-protos/1.17.0/proto-google-common-protos-1.17.0.jar:../../m2/repository/io/grpc/grpc-protobuf-lite/1.28.0/grpc-protobuf-lite-1.28.0.jar:../../m2/repository/io/grpc/grpc-stub/1.28.0/grpc-stub-1.28.0.jar:../../m2/repository/com/opencsv/opencsv/5.3/opencsv-5.3.jar:../../m2/repository/org/apache/commons/commons-lang3/3.11/commons-lang3-3.11.jar:../../m2/repository/org/apache/commons/commons-text/1.9/commons-text-1.9.jar:../../m2/repository/commons-beanutils/commons-beanutils/1.9.4/commons-beanutils-1.9.4.jar:../../m2/repository/commons-logging/commons-logging/1.2/commons-logging-1.2.jar:../../m2/repository/commons-collections/commons-collections/3.2.2/commons-collections-3.2.2.jar:../../m2/repository/org/apache/commons/commons-collections4/4.4/commons-collections4-4.4.jar:../lib/UserProtocolContract-1.0.jar:../lib/HAContract-1.0.jar HDLT_Server"
USER_START_COMMAND="java -Dfile.encoding=UTF-8 -classpath target/classes:../../m2/repository/io/grpc/grpc-netty-shaded/1.28.0/grpc-netty-shaded-1.28.0.jar:../../m2/repository/io/grpc/grpc-core/1.28.0/grpc-core-1.28.0.jar:../../m2/repository/com/google/code/gson/gson/2.8.6/gson-2.8.6.jar:../../m2/repository/com/google/android/annotations/4.1.1.4/annotations-4.1.1.4.jar:../../m2/repository/com/google/errorprone/error_prone_annotations/2.3.4/error_prone_annotations-2.3.4.jar:../../m2/repository/io/perfmark/perfmark-api/0.19.0/perfmark-api-0.19.0.jar:../../m2/repository/io/grpc/grpc-protobuf/1.28.0/grpc-protobuf-1.28.0.jar:../../m2/repository/io/grpc/grpc-api/1.28.0/grpc-api-1.28.0.jar:../../m2/repository/io/grpc/grpc-context/1.28.0/grpc-context-1.28.0.jar:../../m2/repository/com/google/code/findbugs/jsr305/3.0.2/jsr305-3.0.2.jar:../../m2/repository/org/codehaus/mojo/animal-sniffer-annotations/1.18/animal-sniffer-annotations-1.18.jar:../../m2/repository/com/google/protobuf/protobuf-java/3.11.0/protobuf-java-3.11.0.jar:../../m2/repository/com/google/guava/guava/28.1-android/guava-28.1-android.jar:../../m2/repository/com/google/guava/failureaccess/1.0.1/failureaccess-1.0.1.jar:../../m2/repository/com/google/guava/listenablefuture/9999.0-empty-to-avoid-conflict-with-guava/listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar:../../m2/repository/org/checkerframework/checker-compat-qual/2.5.5/checker-compat-qual-2.5.5.jar:../../m2/repository/com/google/j2objc/j2objc-annotations/1.3/j2objc-annotations-1.3.jar:../../m2/repository/com/google/api/grpc/proto-google-common-protos/1.17.0/proto-google-common-protos-1.17.0.jar:../../m2/repository/io/grpc/grpc-protobuf-lite/1.28.0/grpc-protobuf-lite-1.28.0.jar:../../m2/repository/io/grpc/grpc-stub/1.28.0/grpc-stub-1.28.0.jar:../../m2/repository/com/opencsv/opencsv/5.3/opencsv-5.3.jar:../../m2/repository/org/apache/commons/commons-lang3/3.11/commons-lang3-3.11.jar:../../m2/repository/org/apache/commons/commons-text/1.9/commons-text-1.9.jar:../../m2/repository/commons-beanutils/commons-beanutils/1.9.4/commons-beanutils-1.9.4.jar:../../m2/repository/commons-logging/commons-logging/1.2/commons-logging-1.2.jar:../../m2/repository/commons-collections/commons-collections/3.2.2/commons-collections-3.2.2.jar:../../m2/repository/org/apache/commons/commons-collections4/4.4/commons-collections4-4.4.jar:../lib//UserProtocolContract-1.0.jar:../lib//HAContract-1.0.jar HDLT_user "
BYZANTINE_USER_START_COMMAND="java -Dfile.encoding=UTF-8 -classpath target/classes:../../m2/repository/io/grpc/grpc-netty-shaded/1.28.0/grpc-netty-shaded-1.28.0.jar:../../m2/repository/io/grpc/grpc-core/1.28.0/grpc-core-1.28.0.jar:../../m2/repository/com/google/code/gson/gson/2.8.6/gson-2.8.6.jar:../../m2/repository/com/google/android/annotations/4.1.1.4/annotations-4.1.1.4.jar:../../m2/repository/com/google/errorprone/error_prone_annotations/2.3.4/error_prone_annotations-2.3.4.jar:../../m2/repository/io/perfmark/perfmark-api/0.19.0/perfmark-api-0.19.0.jar:../../m2/repository/io/grpc/grpc-protobuf/1.28.0/grpc-protobuf-1.28.0.jar:../../m2/repository/io/grpc/grpc-api/1.28.0/grpc-api-1.28.0.jar:../../m2/repository/io/grpc/grpc-context/1.28.0/grpc-context-1.28.0.jar:../../m2/repository/com/google/code/findbugs/jsr305/3.0.2/jsr305-3.0.2.jar:../../m2/repository/org/codehaus/mojo/animal-sniffer-annotations/1.18/animal-sniffer-annotations-1.18.jar:../../m2/repository/com/google/protobuf/protobuf-java/3.11.0/protobuf-java-3.11.0.jar:../../m2/repository/com/google/guava/guava/28.1-android/guava-28.1-android.jar:../../m2/repository/com/google/guava/failureaccess/1.0.1/failureaccess-1.0.1.jar:../../m2/repository/com/google/guava/listenablefuture/9999.0-empty-to-avoid-conflict-with-guava/listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar:../../m2/repository/org/checkerframework/checker-compat-qual/2.5.5/checker-compat-qual-2.5.5.jar:../../m2/repository/com/google/j2objc/j2objc-annotations/1.3/j2objc-annotations-1.3.jar:../../m2/repository/com/google/api/grpc/proto-google-common-protos/1.17.0/proto-google-common-protos-1.17.0.jar:../../m2/repository/io/grpc/grpc-protobuf-lite/1.28.0/grpc-protobuf-lite-1.28.0.jar:../../m2/repository/io/grpc/grpc-stub/1.28.0/grpc-stub-1.28.0.jar:../../m2/repository/com/opencsv/opencsv/5.3/opencsv-5.3.jar:../../m2/repository/org/apache/commons/commons-lang3/3.11/commons-lang3-3.11.jar:../../m2/repository/org/apache/commons/commons-text/1.9/commons-text-1.9.jar:../../m2/repository/commons-beanutils/commons-beanutils/1.9.4/commons-beanutils-1.9.4.jar:../../m2/repository/commons-logging/commons-logging/1.2/commons-logging-1.2.jar:../../m2/repository/commons-collections/commons-collections/3.2.2/commons-collections-3.2.2.jar:../../m2/repository/org/apache/commons/commons-collections4/4.4/commons-collections4-4.4.jar:../lib//UserProtocolContract-1.0.jar:../lib//HAContract-1.0.jar HDLT_byzantine_user "


NORMAL_OPERATION_FILE = "files/normal_operation_sequence.csv"
NORMAL_BYZANTINE_OPERATION_FILE = "files/normal_byzantine_operation_sequence.csv"
BYZANTINE_OPERATION_FILE = "files/byzantine_operation_sequence.csv"

def compile():
    return_code = subprocess.run([MAVEN_PATH, "clean", "compile"],stdout=subprocess.DEVNULL)
    if return_code.returncode == 0:
        print("INFO: Project successfully compiled!")

def init_server_users(byzantine=False):
    global server
    #server = subprocess.Popen(SERVER_START_COMMAND, shell=True,stdout=subprocess.PIPE,text=True, stdin=subprocess.PIPE, stderr=subprocess.STDOUT,close_fds=True)
    server = pexpect.spawn(SERVER_START_COMMAND)
    #if server.returncode == None:
        #print("INFO: Server started successfully!")

    if byzantine == False:
        users = NUMBER_USERS
    else:
        users = NUMBER_USERS - NUMBER_BYZANTINE_USERS
    
    for i in range(users):
        command = USER_START_COMMAND +  'u' + str(i+1)
        p = subprocess.Popen(command,shell=True, text=True,stdout=subprocess.PIPE, stdin=subprocess.PIPE, stderr=subprocess.STDOUT)
        p.stdin.write('e 0\n')
        p.stdin.flush()
        user_process_array.append(p)
        if p.returncode == None:
            print("INFO: User " + str(i+1) + " successfully started!")
    
    #Starts byzantine users if flag is true
    if byzantine == True:
        for i in range(NUMBER_BYZANTINE_USERS):
            command = BYZANTINE_USER_START_COMMAND +  'u' + str(NUMBER_USERS-NUMBER_BYZANTINE_USERS+i+1)
            p = pexpect.spawn(command)           
            byzantine_user_process_array.append(p)
            if p is not None:
                print("INFO: Byzantine User " + str(NUMBER_USERS-NUMBER_BYZANTINE_USERS+i+1) + " successfully started!")

def close_server_users(byzantine = False):
    for i in user_process_array:\
        i.kill()
    
    if byzantine==True:
        for i in byzantine_user_process_array:\
            i.terminate()

    global server
    server.terminate()

def normal_operation():
    os.remove(LOCATION_REPORT_FILE)
    compile()   
    init_server_users()

    #Executes deterministic steps
    
    #Reads operations file and executes
    print("\n")
    with open(NORMAL_OPERATION_FILE) as csv_file:
        csv_reader =  csv.reader(csv_file, delimiter=',')
        for row in csv_reader:
            epoch = row[0]
            user = int(row[1])
            command = str(row[2])
            p  = user_process_array[user-1]
            p.stdin.write((str('e ' + epoch) + '\n'))
            p.stdin.flush()
            p.stdout.flush()
            p.stdin.write((command + '\n'))
            p.stdin.flush()
            p.stdout.flush()
            if command == 's':
                server.expect("INFO: Location report submitted!" or "ERROR: Location report invalid!")
                print("INFO: Location report for user " + str(user) + " at epoch " + str(epoch) + " submitted!")
            
    #Shows location report file
    print("\n-> Printing the content of `location_reports` file\n")
    time.sleep(1)
    f = open(LOCATION_REPORT_FILE, 'r')
    print(f.read())
    f.close()




    close_server_users()

def normal_byzantine_operation():
    os.remove(LOCATION_REPORT_FILE)
    compile()   
    init_server_users(byzantine=True)
     #Reads operations file and executes
    print("\n")
    with open(NORMAL_BYZANTINE_OPERATION_FILE) as csv_file:
        csv_reader =  csv.reader(csv_file, delimiter=',')
        for row in csv_reader:
            user_type = str(row[0])
            epoch = row[1]
            command = str(row[3])
            user = int(row[2])
            if user_type == 'u':
                p  = user_process_array[user-1]

            elif user_type == 'b':
                p = byzantine_user_process_array[user - (NUMBER_USERS - NUMBER_BYZANTINE_USERS) -1 ]

            if command == 's' or command == 'r':
                p.stdin.write((str('e ' + epoch) + '\n'))
                p.stdin.flush()
                p.stdout.flush()

                p.stdin.write((command + '\n'))
                p.stdin.flush()
                p.stdout.flush()
                if command == 's':
                    server.expect("INFO: Location report submitted!" or "ERROR: Location report invalid!")
                    print("INFO: Location report for user " + str(user) + " at epoch " + str(epoch) + " submitted!")
                

            if command[0] == 'a':
                p.sendline((str('e ' + epoch)))
                p.sendline(command)
                p.expect("INFO: Attack " + command[1] + " finished!")
                print("INFO: Attack " + command[1] + " finished!")
   
    #Shows location report file
    print("\n-> Printing the content of `location_reports` file\n")
    time.sleep(1)
    f = open(LOCATION_REPORT_FILE, 'r')
    print(f.read())
    f.close()



    close_server_users(byzantine=True)


def byzantine_operation():
    compile()   
    init_server_users(byzantine=True)
    os.remove(LOCATION_REPORT_FILE)
     #Reads operations file and executes
    print("\n")
    with open(BYZANTINE_OPERATION_FILE) as csv_file:
        csv_reader =  csv.reader(csv_file, delimiter=',')
        for row in csv_reader:
            epoch = row[1]
            command = str(row[3])
            user = int(row[2])

            p = byzantine_user_process_array[user - (NUMBER_USERS - NUMBER_BYZANTINE_USERS) -1 ]
            
            if command[0] == 'a':
                p.sendline((str('e ' + epoch)))
                p.sendline(command)
                p.expect("INFO: Attack " + command[1] + " finished!")
                print("INFO: Attack " + command[1] + " finished!")
    #Shows location report file
    print("\n-> Printing the content of `location_reports` file\n")
    time.sleep(1)
    f = open(LOCATION_REPORT_FILE, 'r')
    print(f.read())
    f.close()


    close_server_users(byzantine=True)
    


def main():
    option = 0
    switcher = {
        '1' : lambda: normal_operation(),
        '2' : lambda: normal_byzantine_operation(),
        '3' : lambda: byzantine_operation(),
        '4' : lambda: close_server_users() and exit()
    }

    while(option != '4'):
        print("\n---  HDLT Project Stage 1 Tester ---\n")
        print("This program executes a series of Java Instances to test the server and user components")
        print("---------------------------------------------------------------------------------------\n")
        print("1 - Run normal user and server operation (request proof and submit location)")
        print("2 - Run normal user and server operation along with custom and deterministic byzantine user tests")
        print("3 - Run only custom and deterministic byzantine user tests")
        print("4 - Exit the tester")
        option = input("\nSelect one of the above options: ")

        func = switcher.get(option,lambda: "\nERROR: Wrong option!\n")
        func()
        

if __name__ == "__main__":
    main()