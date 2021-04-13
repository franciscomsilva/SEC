import sys,subprocess
from subprocess import Popen

NUMBER_USERS = 8
NUMBER_BYZANTINE_USERS = 3
MAVEN_PATH = "../../maven/bin/mvn"
user_process_array = []

SERVER_START_COMMAND="java -Dfile.encoding=UTF-8 -classpath target/classes:../../m2/repository/io/grpc/grpc-netty-shaded/1.28.0/grpc-netty-shaded-1.28.0.jar:../../m2/repository/io/grpc/grpc-core/1.28.0/grpc-core-1.28.0.jar:../../m2/repository/com/google/code/gson/gson/2.8.6/gson-2.8.6.jar:../../m2/repository/com/google/android/annotations/4.1.1.4/annotations-4.1.1.4.jar:../../m2/repository/com/google/errorprone/error_prone_annotations/2.3.4/error_prone_annotations-2.3.4.jar:../../m2/repository/io/perfmark/perfmark-api/0.19.0/perfmark-api-0.19.0.jar:../../m2/repository/io/grpc/grpc-protobuf/1.28.0/grpc-protobuf-1.28.0.jar:../../m2/repository/io/grpc/grpc-api/1.28.0/grpc-api-1.28.0.jar:../../m2/repository/io/grpc/grpc-context/1.28.0/grpc-context-1.28.0.jar:../../m2/repository/com/google/code/findbugs/jsr305/3.0.2/jsr305-3.0.2.jar:../../m2/repository/org/codehaus/mojo/animal-sniffer-annotations/1.18/animal-sniffer-annotations-1.18.jar:../../m2/repository/com/google/protobuf/protobuf-java/3.11.0/protobuf-java-3.11.0.jar:../../m2/repository/com/google/guava/guava/28.1-android/guava-28.1-android.jar:../../m2/repository/com/google/guava/failureaccess/1.0.1/failureaccess-1.0.1.jar:../../m2/repository/com/google/guava/listenablefuture/9999.0-empty-to-avoid-conflict-with-guava/listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar:../../m2/repository/org/checkerframework/checker-compat-qual/2.5.5/checker-compat-qual-2.5.5.jar:../../m2/repository/com/google/j2objc/j2objc-annotations/1.3/j2objc-annotations-1.3.jar:../../m2/repository/com/google/api/grpc/proto-google-common-protos/1.17.0/proto-google-common-protos-1.17.0.jar:../../m2/repository/io/grpc/grpc-protobuf-lite/1.28.0/grpc-protobuf-lite-1.28.0.jar:../../m2/repository/io/grpc/grpc-stub/1.28.0/grpc-stub-1.28.0.jar:../../m2/repository/com/opencsv/opencsv/5.3/opencsv-5.3.jar:../../m2/repository/org/apache/commons/commons-lang3/3.11/commons-lang3-3.11.jar:../../m2/repository/org/apache/commons/commons-text/1.9/commons-text-1.9.jar:../../m2/repository/commons-beanutils/commons-beanutils/1.9.4/commons-beanutils-1.9.4.jar:../../m2/repository/commons-logging/commons-logging/1.2/commons-logging-1.2.jar:../../m2/repository/commons-collections/commons-collections/3.2.2/commons-collections-3.2.2.jar:../../m2/repository/org/apache/commons/commons-collections4/4.4/commons-collections4-4.4.jar:../lib/UserProtocolContract-1.0.jar:../lib/HAContract-1.0.jar HDLT_Server"
USER_START_COMMAND="java -Dfile.encoding=UTF-8 -classpath target/classes:../../m2/repository/io/grpc/grpc-netty-shaded/1.28.0/grpc-netty-shaded-1.28.0.jar:../../m2/repository/io/grpc/grpc-core/1.28.0/grpc-core-1.28.0.jar:../../m2/repository/com/google/code/gson/gson/2.8.6/gson-2.8.6.jar:../../m2/repository/com/google/android/annotations/4.1.1.4/annotations-4.1.1.4.jar:../../m2/repository/com/google/errorprone/error_prone_annotations/2.3.4/error_prone_annotations-2.3.4.jar:../../m2/repository/io/perfmark/perfmark-api/0.19.0/perfmark-api-0.19.0.jar:../../m2/repository/io/grpc/grpc-protobuf/1.28.0/grpc-protobuf-1.28.0.jar:../../m2/repository/io/grpc/grpc-api/1.28.0/grpc-api-1.28.0.jar:../../m2/repository/io/grpc/grpc-context/1.28.0/grpc-context-1.28.0.jar:../../m2/repository/com/google/code/findbugs/jsr305/3.0.2/jsr305-3.0.2.jar:../../m2/repository/org/codehaus/mojo/animal-sniffer-annotations/1.18/animal-sniffer-annotations-1.18.jar:../../m2/repository/com/google/protobuf/protobuf-java/3.11.0/protobuf-java-3.11.0.jar:../../m2/repository/com/google/guava/guava/28.1-android/guava-28.1-android.jar:../../m2/repository/com/google/guava/failureaccess/1.0.1/failureaccess-1.0.1.jar:../../m2/repository/com/google/guava/listenablefuture/9999.0-empty-to-avoid-conflict-with-guava/listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar:../../m2/repository/org/checkerframework/checker-compat-qual/2.5.5/checker-compat-qual-2.5.5.jar:../../m2/repository/com/google/j2objc/j2objc-annotations/1.3/j2objc-annotations-1.3.jar:../../m2/repository/com/google/api/grpc/proto-google-common-protos/1.17.0/proto-google-common-protos-1.17.0.jar:../../m2/repository/io/grpc/grpc-protobuf-lite/1.28.0/grpc-protobuf-lite-1.28.0.jar:../../m2/repository/io/grpc/grpc-stub/1.28.0/grpc-stub-1.28.0.jar:../../m2/repository/com/opencsv/opencsv/5.3/opencsv-5.3.jar:../../m2/repository/org/apache/commons/commons-lang3/3.11/commons-lang3-3.11.jar:../../m2/repository/org/apache/commons/commons-text/1.9/commons-text-1.9.jar:../../m2/repository/commons-beanutils/commons-beanutils/1.9.4/commons-beanutils-1.9.4.jar:../../m2/repository/commons-logging/commons-logging/1.2/commons-logging-1.2.jar:../../m2/repository/commons-collections/commons-collections/3.2.2/commons-collections-3.2.2.jar:../../m2/repository/org/apache/commons/commons-collections4/4.4/commons-collections4-4.4.jar:../lib//UserProtocolContract-1.0.jar:../lib//HAContract-1.0.jar HDLT_user "

def compile():
    return_code = subprocess.run([MAVEN_PATH, "clean", "compile"],stdout=subprocess.DEVNULL)
    if return_code.returncode == 0:
        print("INFO: Project successfully compiled!")

def init_server_users():
    global server
    server = Popen(SERVER_START_COMMAND, shell=True,stdout=subprocess.DEVNULL, stdin=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    if server.returncode == None:
        print("INFO: Server started successfully!")

    
    for i in range(NUMBER_USERS - NUMBER_BYZANTINE_USERS):
        command = USER_START_COMMAND +  'u' + str(i+1)
        p = Popen(command,shell=True,stdout=subprocess.DEVNULL, stdin=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        p.communicate(input=bytes('nothing','utf-8'))
        user_process_array.append(p)
        if p.returncode == 0:
            print("INFO: User " + str(i+1) + " successfully started!")


def close_server_users():
    for i in user_process_array:
        print(i)
        i.terminate()
    global server
    server.terminate()


def normal_operation():
    compile()   
    init_server_users()

    #Executes deterministic steps


    close_server_users()


def main():
    option = 0
    switcher = {
        '1' : lambda: normal_operation(),
        '4' : lambda: exit()
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