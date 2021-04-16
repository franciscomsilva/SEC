import sys,subprocess,csv,pexpect,time,os,json


NUMBER_USERS = 10
NUMBER_BYZANTINE_USERS = 3
MAVEN_PATH = "../../maven/bin/mvn"
LOCATION_REPORT_FILE = "files/location_reports.json"

user_process_array = []
byzantine_user_process_array = []


SERVER_START_COMMAND="java -cp ../lib/UserProtocolContract-1.0.jar:../lib/HAContract-1.0.jar:target/HDLT-1.0-SNAPSHOT-jar-with-dependencies.jar HDLT_Server "
USER_START_COMMAND="java -cp ../lib/UserProtocolContract-1.0.jar:target/HDLT-1.0-SNAPSHOT-jar-with-dependencies.jar HDLT_user "
BYZANTINE_USER_START_COMMAND="java -cp ../lib/UserProtocolContract-1.0.jar:target/HDLT-1.0-SNAPSHOT-jar-with-dependencies.jar HDLT_byzantine_user "


NORMAL_OPERATION_FILE = "files/normal_operation_sequence.csv"
NORMAL_BYZANTINE_OPERATION_FILE = "files/normal_byzantine_operation_sequence.csv"
BYZANTINE_OPERATION_FILE = "files/byzantine_operation_sequence.csv"

def compile():
    return_code = subprocess.run([MAVEN_PATH, "package"],stdout=subprocess.DEVNULL)
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
    user_process_array.clear()
    byzantine_user_process_array.clear()

    global server
    server.terminate()

def normal_operation():
    if os.path.isfile(LOCATION_REPORT_FILE):
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

            if command == 'o':
                if len(row) < 4:
                    exit("ERROR: Wrong syntax in command file")
                command += " " + str(row[3])

            p.stdin.write(command + '\n')
            p.stdin.flush()
            p.stdout.flush()
    

            if command == 's':
                server.expect(["INFO: Location report submitted!","ERROR: Location report invalid!","ERROR: Invalid request"])
                print(server.after)
            if command[0] == 'o':
                server.expect(["INFO: Sent location report for u"+ str(user) + " at epoch " + str(row[3]), "ERROR: No location report for that user in that epoch!", "ERROR: Invalid key"])
                print(server.after)

          
                    
            
    #Shows location report file
    print("\n-> Printing the content of `location_reports` file\n")
    time.sleep(1)
    f = open(LOCATION_REPORT_FILE, 'r')
    data = json.load(f)
    print(json.dumps(data,indent=2))
    f.close()




    close_server_users()

def normal_byzantine_operation():
    if os.path.isfile(LOCATION_REPORT_FILE):
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

            if command == 's' or command == 'r' or command == 'o':
                p.stdin.write((str('e ' + epoch) + '\n'))
                p.stdin.flush()
                p.stdout.flush()

                if command == 'o':
                    if len(row) < 4:
                        exit("ERROR: Wrong syntax in command file")
                    command += " " + str(row[4])
                p.stdin.write((command + '\n'))
                p.stdin.flush()
                p.stdout.flush()
                if command == 's':
                    server.expect(["INFO: Location report submitted!","ERROR: Location report invalid!","ERROR: Invalid request"])
                    print(server.after)                

            if command[0] == 'a':
                p.sendline((str('e ' + epoch)))
                p.sendline(command)
                p.expect("INFO: Attack " + command[1] + " finished!")
                print("INFO: Attack " + command[1] + " finished!")

            if command[0] == 'o':
                server.expect(["INFO: Sent location report for u"+ str(user) + " at epoch " + str(row[3]), "ERROR: No location report for that user in that epoch!", "ERROR: Invalid key"])
                print(server.after)
   
   #Shows location report file
    print("\n-> Printing the content of `location_reports` file\n")
    time.sleep(1)
    f = open(LOCATION_REPORT_FILE, 'r')
    data = json.load(f)
    print(json.dumps(data,indent=2))
    f.close()



    close_server_users(byzantine=True)




def main():
    option = 0
    switcher = {
        '1' : lambda: normal_operation(),
        '2' : lambda: normal_byzantine_operation(),
        '3' : lambda: exit()
    }

    while(option != '4'):
        print("\n---  HDLT Project Stage 1 Tester ---\n")
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