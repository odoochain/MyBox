rem Unzip source package. Edit this script to change directories as your env.
set version=6.4.2
set jpackagePath=D:\Programs\Java\openjdk-15.0.1\bin
set jdkPath=D:\Programs\Java\openjdk-15.0.1

rd/Q/S    app\
rd/Q/S    jar\
md  app  jar
cd ../..
call mvn clean
call mvn package
call move target\*.jar  pack\win\jar\
rd/Q/S    target\
cd pack\win
%jpackagePath%\jpackage --type app-image   --app-version %version% --vendor Mara --verbose --runtime-image  %jdkPath%  --dest app --name MyBox --input jar --main-jar MyBox-%version%.jar --icon res\MyBox.ico
copy res\*.txt app\MyBox
move jar\*.jar  .
