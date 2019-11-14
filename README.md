# AsyncChecker
A flow, field, object, context and path-sensitive inter-procedural static analysis tool for misuse detection of AsyncTask

we can use it through following command:

    java -jar AsyncChecker.jar [options]

The command line options of AsyncChecker are shown as following:
    
    -apkName	    The name of apk under analysis. For example, douyin.apk.
    -javaHome       The directory where rt.jar lies. For example, C:\\java\\lib\\rt.jar.
    -apkBasePath	    The directory where apk file lies. For example, D:\\test.
    -outputBasePath     The directory where the output file of AsyncChecker lies. 
    -androidPath	    The directory where android.jar lies.
    -debugMode          Indicate whether AsyncChecker is in debug mode. Debug mode will output detailed path message which is time consuming. 1 means yes, 0 means no. 0 by default.
    -isJimpleOutput	    Indicate whether AsyncChecker output jimple file or not. 1 means yes, 0 means no. 1 by default.
    -maxRunningTime	    The maximum running time of AsyncChecker. 1800000 milliseconds (30 minutes) by default.
    -maxLoopUnrollNumber      The maximum unroll time when dealing with loop structure. 1 by default.
    -maxPathNumber      The maximum path number during analysis. 40000 by default.
    -maxRecursiveInvocationLevel      The maximum number of recursive invocation. 0 by default.
    -entryMethod      entry method of analysis. dummy main method of Activity by default. If you want to set it, please input the complete method name. For example, com.test.testMethod
    -initMethodSubsignature       If entry method is a non-static method, we need to indicate the init method of object that entry method belongs to. Non-parametric init method by default. The format should be subsignature in jimple. For example, void <init>(int).
    -configureFile      The path of configure file. For example, D:\\configuration.txt

Note that -apkName, -javaHome, -apkBasePath, -outputBasePath and -androidPath are compulsory options, which means they must be set. For example:

    java -jar AsyncChecker.jar -apkName douyin.apk -javaHome C:\\Program Files\\Java\\rt.jar -apkBasePath D:\\test -outputBasePath D:\\apkPath -androidPath D:\\android-platform
    
If we set -configureFile, then the configuration item in the configure file will cover the configure item set in command line. An example  of configure file is shown as following:
    
    apkName  BasicTypeTest.apk
    apkBasePath  test-apk
    outputBasePath  jimple\\test-apk
    isJimpleOutput  1
    androidPath  D:\\download\\soot-path\\android-platforms-master
    maxRunningTime
    maxLoopUnrollNumber
    maxPathNumber
    maxRecursiveInvocationLevel
    entryMethod com.example.basictypetest.MainActivity.testCastExpr
    initMethodSubsignature 
    com.example.arraytest.MainActivity.testArrayParam

Each line in the file denotes a configuration. Configuration item and its value are separated by blank space. If configuration item is illegal or the value is null, then the configuration won't take effect. In the above file, only the first five lines will take effect.

We can make configuration in file through the following command:

    java -jar AsyncChecker.jar -configureFile D:\\configuration.txt
