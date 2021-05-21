import sys,subprocess,csv,pexpect,time,os,json,signal


NUMBER_USERS = 10
NUMBER_SERVERS = 4
NUMBER_BYZANTINE_SERVERS = 1
NUMBER_BYZANTINE_USERS = 3
MAVEN_PATH = "../../maven/bin/mvn"
LOCATION_REPORT_FILE = "files/location_reports"

user_process_array = []
byzantine_user_process_array = []


server_process_array = []
byzantine_server_process_array = []

SERVER_START_COMMAND="java -cp ../lib/UserProtocolContract-1.0.jar:../lib/HAContract-1.0.jar:target/HDLT-1.0-SNAPSHOT-jar-with-dependencies.jar HDLT_Server "
BYZANTINE_SERVER_START_COMMAND="java -cp ../lib/UserProtocolContract-1.0.jar:../lib/HAContract-1.0.jar:target/HDLT-1.0-SNAPSHOT-jar-with-dependencies.jar HDLT_Byzantine_Server "
USER_START_COMMAND="java -cp ../lib/UserProtocolContract-1.0.jar:target/HDLT-1.0-SNAPSHOT-jar-with-dependencies.jar HDLT_user "
BYZANTINE_USER_START_COMMAND="java -cp ../lib/UserProtocolContract-1.0.jar:target/HDLT-1.0-SNAPSHOT-jar-with-dependencies.jar HDLT_byzantine_user "


NORMAL_OPERATION_FILE = "files/normal_operation_sequence.csv"
NORMAL_BYZANTINE_OPERATION_FILE = "files/normal_byzantine_operation_sequence.csv"
BYZANTINE_OPERATION_FILE = "files/byzantine_operation_sequence.csv"

def compile():
    return_code = subprocess.run([MAVEN_PATH, "package"],stdout=subprocess.DEVNULL)
    time.sleep(2)
    if return_code.returncode == 0:
        print("INFO: Project successfully compiled!")

def init_server_users(user_byzantine=False, server_byzantine = False):

    if user_byzantine == False:
        users = NUMBER_USERS
    else:
        users = NUMBER_USERS - NUMBER_BYZANTINE_USERS

    if server_byzantine == False:
        servers = NUMBER_SERVERS
    else:
        servers = NUMBER_SERVERS - NUMBER_BYZANTINE_SERVERS
        

    for i in range(servers):
        command = SERVER_START_COMMAND + str(i+1)  +" &> files/outputs/server" + str(i+1) + ".output";
        server =subprocess.Popen(command, shell=True,stdout=subprocess.PIPE,text=True, stdin=subprocess.PIPE, stderr=subprocess.STDOUT,preexec_fn=os.setsid,close_fds=True)
        server.stdin.write("server" + str(i+1) + "\n")
        server.stdin.flush()
        server_process_array.append(server)
        if server.returncode == 0:
            print("INFO: Server " + str(i+1) + " successfully started!")
        time.sleep(1)


    #Starts byzantine servers if flag is true
    if server_byzantine == True:
        for i in range(NUMBER_BYZANTINE_SERVERS):
            command = BYZANTINE_SERVER_START_COMMAND + str(NUMBER_SERVERS - NUMBER_BYZANTINE_SERVERS + i+1)  +" &> files/outputs/server" + str(NUMBER_SERVERS - NUMBER_BYZANTINE_SERVERS + i+1) + ".output";
            server =subprocess.Popen(command, shell=True,stdout=subprocess.PIPE,text=True, stdin=subprocess.PIPE, stderr=subprocess.STDOUT,preexec_fn=os.setsid,close_fds=True)
            server.stdin.write("server" + str(NUMBER_SERVERS - NUMBER_BYZANTINE_SERVERS + i+1) + "\n")
            server.stdin.flush()
            byzantine_server_process_array.append(server)
            if server.returncode == 0:
                print("INFO: Server Byzantine " + str(NUMBER_SERVERS - NUMBER_BYZANTINE_SERVERS + i+1) + " successfully started!")
            time.sleep(1)


    for i in range(users):
        command = USER_START_COMMAND +  'client' + str(i+1) +" &> files/outputs/client" + str(i+1) + ".output";
        p = subprocess.Popen(command,shell=True, text=True,stdout=subprocess.PIPE, stdin=subprocess.PIPE, stderr=subprocess.STDOUT,preexec_fn=os.setsid)
        p.stdin.write("client" + str(i+1) + "\n")
        p.stdin.flush()
        user_process_array.append(p)
        if p.returncode == 0:
            print("INFO: Client " + str(i+1) + " successfully started!")
        time.sleep(1)

    #Starts byzantine users if flag is true
    if user_byzantine == True:
        for i in range(NUMBER_BYZANTINE_USERS):
            command = USER_START_COMMAND +  'client' + str(i+1) +" &> files/outputs/client" + str(i+1) + ".output";
            p = subprocess.Popen(command,shell=True, text=True,stdout=subprocess.PIPE, stdin=subprocess.PIPE, stderr=subprocess.STDOUT,preexec_fn=os.setsid)
            p.stdin.write("client" + str(i+1) + "\n")
            p.stdin.flush() 
            byzantine_user_process_array.append(p)
            if p.returncode == 0:
                print("INFO: Byzantine User " + str(NUMBER_USERS-NUMBER_BYZANTINE_USERS+i+1) + " successfully started!")


def close_server_users():

    print("INFO: Closing java instances")
    for i in user_process_array:
        os.killpg(os.getpgid(i.pid), signal.SIGTERM)
        time.sleep(1)

    for i in server_process_array:
        os.killpg(os.getpgid(i.pid), signal.SIGTERM)
        time.sleep(1)
    
    for i in byzantine_user_process_array:
        os.killpg(os.getpgid(i.pid), signal.SIGTERM)
        time.sleep(1)

    for i in byzantine_server_process_array:
        os.killpg(os.getpgid(i.pid), signal.SIGTERM)
        time.sleep(1)

    user_process_array.clear()
    byzantine_user_process_array.clear()
    server_process_array.clear()
    byzantine_server_process_array.clear()


def normal_operation():
    remove_reports_files()
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
            time.sleep(1)

            if command == 'o':
                if len(row) < 4:
                    exit("ERROR: Wrong syntax in command file")
                command += " " + str(row[3])
            p.stdin.write(command + '\n')
            p.stdin.flush()
            time.sleep(1)
    
    print("\n\n ALL OPERATIONS TERMINATED - CHECK THE OUTPUT FILES\n\n")
    

    close_server_users()

def normal_byzantine_operation():
    remove_reports_files()
    compile()   
    init_server_users(user_byzantine=False, server_byzantine = True)
     #Reads operations file and executes
    print("\n")
    with open(NORMAL_BYZANTINE_OPERATION_FILE) as csv_file:
        csv_reader =  csv.reader(csv_file, delimiter=',')
        for row in csv_reader:
            node = str(row[0])

            if node == 'b':
                server_number = int(row[1])
                s = byzantine_server_process_array[server_number - 1]
                
                command = str(row[2])
                print("INFO: Executing byzantine server attack " + command);
                s.stdin.write((command + '\n'))
                s.stdin.flush()
                s.stdout.flush()
                time.sleep(1)

            elif node == 'u':
                epoch = row[1]
                user = int(row[2])
                command = str(row[3])
                
                p  = user_process_array[user-1]

                if command == 's' or command == 'r' or command == 'o' or command == 'p':
                    p.stdin.write((str('e ' + epoch) + '\n'))
                    p.stdin.flush()
                    p.stdout.flush()


                    if command == 'o':
                        if len(row) < 4:
                            exit("ERROR: Wrong syntax in command file")
                        command += " " + str(row[4])

                    if command == 'p':
                        if len(row) < 4:
                            exit("ERROR: Wrong syntax in command file")
                        command +=  " "
                        
                        for i in range(len(row) - 4):
                            command += str(row[4 + i]) + ","
                        command = command[:-1]
                    
                    p.stdin.write((command + '\n'))
                    p.stdin.flush()
                    p.stdout.flush()
                    time.sleep(1)


        

       
    print("\n\n ALL OPERATIONS TERMINATED - CHECK THE OUTPUT FILES\n\n")
    
    close_server_users()

def print_reports_files():
    for i in range(NUMBER_SERVERS):
        if os.path.isfile(LOCATION_REPORT_FILE + str(i+1) + ".json"):
            f = open(LOCATION_REPORT_FILE + str(i+1) + ".json", 'r')
            data = json.load(f)
            print(json.dumps(data,indent=2))
            f.close()



def remove_reports_files():
    for i in range(NUMBER_SERVERS):
        if os.path.isfile(LOCATION_REPORT_FILE + str(i+1) + ".json"):
            os.remove(LOCATION_REPORT_FILE + str(i+1) + ".json")  


def main():
    option = 0
    switcher = {
        '1' : lambda: normal_operation(),
        '2' : lambda: normal_byzantine_operation(),
        '3' : lambda: exit()
    }

    while(option != '4'):
        print("\n---  HDLT Project Tester ---\n")
        print("This program executes a series of Java Instances to test the server and user components")
        print("---------------------------------------------------------------------------------------\n")
        print("1 - Run normal user and server operation (request proof and submit location)")
        print("2 - Run normal user and server operation along with custom and deterministic byzantine user tests")
        print("3 - Exit the tester")
        option = input("\nSelect one of the above options: ")

        func = switcher.get(option,lambda: "\nERROR: Wrong option!\n")
        func()
        

if __name__ == "__main__":
    main()