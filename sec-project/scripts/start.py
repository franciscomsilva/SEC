import sys,subprocess

NUMBER_USERS = 10
NUMBER_BYZANTINE_USERS = 3
JAVA_PATH = "../../java/bin/java"


def init_server_users():
    print("hello")

def switcher(i):
    switcher = {
        '1' : init_server_users,
        '4' : exit()
    }
    return switcher.get(i,"\nERROR: Wrong option!\n")()

def main():
    option = 0;

    while(option != '4'):
        print("\n---  HDLT Project Stage 1 Tester ---\n")
        print("This program executes a series of Java Instances to test the server and user components")
        print("---------------------------------------------------------------------------------------\n")
        print("1 - Run normal user and server operation (request proof and submit location)")
        print("2 - Run normal user and server operation along with custom and deterministic byzantine user tests")
        print("3 - Run only custom and deterministic byzantine user tests")
        print("4 - Exit the tester")
        option = input("\nSelect one of the above options: ")

        func = switcher(option)
        func()

if __name__ == "__main__":
    main()