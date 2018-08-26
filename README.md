# Altruix IS

## How to build (if Gradle is installed)

`gradle --no-daemon installDist`

## How to build (if Gradle is not installed)

`gradlew --no-daemon installDist`


## How to run

`start2.sh`

## How to shut down

1. Find out the process ID of the app using `ps -ef | grep -i java`.
2. `kill -15 <PID>` where `<PID>` is the process ID you found in previous step.

## How to run the tests

`gradle test`

## MongoDB resources

* http://mongodb.github.io/mongo-java-driver/3.4/driver/getting-started/quick-start/
* https://docs.mongodb.com/ecosystem/drivers/java/
* http://mongodb.github.io/mongo-java-driver/3.4/javadoc/?com/mongodb/MongoClient.html

## Apache POI

* https://poi.apache.org/spreadsheet/how-to.html#sxssf

## Amazon Ad Companion

I wanted to create a tool that would allow me to create better Amazon ads. The prototype
of it can be found in `cc.altruix.is1.adr`.

You can use in the following way.

1. Update `cc.altruix.is1.adr.AmazonAdCompanionTests.Companion.getSampleAds` so that it matches your actual data.
2. Create a copy of test `cc.altruix.is1.adr.AmazonAdCompanionTests.spike`, which uses the data from previous step.
3. Run the test in debug mode.
4. Save the results of the expression `termsToCsv(terms)` in a CSV file (like `AmazonAdCompanionTests.termsTfIdf.exp.csv`).
5. Open the CSV file. Lines with highest number in the `Beta (slope)` contain the most relevant words (i. e. if you include these words, the reward of the ad should be higher, according to simple regression results).

### Further reading

* See file `README.md` in `src/main/cc/altruix/is1/adr`.