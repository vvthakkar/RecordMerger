rm -rf build
mkdir build

javac -source 1.8 -target 1.8 -sourcepath src -cp ".:lib/*" -d build src/RecordMerger.java

rm -f cantest.jar
jar cvf cantest.jar -C build/ .

rm -f veeva_solution.zip
jar cvfM veeva_solution.zip .

