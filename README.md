# Highly Dependable Location Tracker

We made bash scripts to help build the system and make it easier to run the system. Also, we made a Python script that launchs each component of the system and allows to make automated tests, namely attacks by byzantine users.
Therefore, the following instructions must be applied in a Linux environment.

## Move to the work directory

```shell
$ cd code/HDLT
```

## Build the project

```shell
$ chmod +x compile.sh
$ ./compile.sh
```

## Run the Python script with automated tests

```shell
$ pip3 install pexpect
$ python3 start.py
```

## Run the system manually

### Launch Server
```shell
$ chmod +x lauchServer.sh
$ ./lauchServer.sh
```
### Launch Clients
```shell
$ chmod +x lauchClient.sh
$ ./lauchClient.sh <client_id>
```
### Launch Byzantine Clients
```shell
$ chmod +x lauchByzantine.sh
$ ./lauchByzantine.sh <byzantine_id>
```

### Launch HA CLient
```shell
$ chmod +x lauchHA.sh
$ ./lauchHA.sh
```