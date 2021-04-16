import random
import csv

users = [1,2,3,4,5,6,7,8,9,10]
n_epochs = 10
epoch_counter = 0
last_epoch = []

with open('locations.csv', mode='w') as locations_file:
    locations_writer = csv.writer(locations_file, delimiter=',', quotechar='"', quoting=csv.QUOTE_MINIMAL)

    for i in range(n_epochs):
        for j in users:
            if (random.randint(0,10) == random.randint(0,10)) and len(last_epoch) > j:
                x = last_epoch[j][0]
                y = last_epoch[j][1]
            else:
                x = random.randint(0,4)
                y = random.randint(0,4)

            location_epoch = ["u"+str(j), i, x, y]
            last_epoch.insert(j,[x,y])

            locations_writer.writerow(location_epoch)
        
