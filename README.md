# RecordMerger

Overview

Given the 2 sample files: "first.html" and "second.csv" under the "data" directory, write a Java program that performs the following:

1.In first.html it contains a table called "directory". Read and extract the table.


2.Read and extract the table from second.csv.


3.Combine and merge these 2 tables into 1 by consolidating duplicated columns and write the results in a file named "combined.csv".
◦For the merge operation, assume "ID" is unique.
◦The "ID" column in the output file "combined.csv" should be sorted in ascending order.
◦The column headers do not need to be in multiple languages.


When in doubt, state your assumptions and proceed.

Your design must take into consideration of future support of new input file formats (for example xml files).

Note 1: DO NOT MODIFY THE GIVEN DATA FILES! Note 2: you can include any open-source libraries you need in addition to what's provided in "lib" directory. Note 3: Use the given RecordMerger class as a starting point. Note 4: Please refrain from using language features from Java 9 or above. The compiler flag is set to accept 1.8 source.

Expected Outcome
•Your program must generate "combined.csv" DONE
•"combined.csv" must contain a header row and no duplicate IDs.
•the IDs in "combined.csv" must be sorted in ascending order.

Important!!!
•Your program should be extensible and easy to maintain. Approach it as if you need to ship a product with this code.
•Additional file types will need to be supported in the future (not yet). Your code must make it easy to add new file types.
•The sample data files are just samples. Out of the box, your program must work with: a. any number of input files. b. any number of columns in the csv or html files. c. the data can be in any language (Chinese, French, English, etc).
