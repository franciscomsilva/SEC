# Highly Dependable Location Tracker

We made bash scripts to help build the system and make it easier to run the system. Also, we made a Python script that launchs each component of the system and allows to make automated tests, namely attacks by byzantine users.
Therefore, the following instructions must be applied in a Linux environment.

## Move to the work directory

```
$ cd code/HDLT
```

## Build the project

```
$ chmod +x compile.sh
$ ./compile.sh
```

## Run the Python script with automated tests

```
$ pip3 install pexpect
$ python3 start.py
```

## Run the system manually

### Launch Server
```
$ chmod +x launchServer.sh
$ ./launchServer.sh
```
### Launch Clients
```
$ chmod +x launchClient.sh
$ ./launchClient.sh <client_id>
```
### Launch Byzantine Clients
```
$ chmod +x launchByzantine.sh
$ ./launchByzantine.sh <byzantine_id>
```

### Launch HA CLient
```
$ chmod +x launchHA.sh
$ ./launchHA.sh
```