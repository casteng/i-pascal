Intellij IDEA Pascal plugin
===========================

Features
--------
 - includes

Installation
-------------

1. Select File->Settings->Plugins
2. Click install plugin from disk
3. Choose `IdeaPas.zip`

Creating new project
---------------------

In order to compile Pascal applications a corresponding type of SDK need to be created in IDEA.

1. Select File->New project
2. Select "Pascal Module"
3. Click "New" to the right of "Project SDK" box
4. Choose directory where FPC compiler is installed

To adjust SDK settings:
1. Select File->Project Structure
2. Choose SDKs
3. Click on desired SDK

Compiling and running
---------------------

To compile current file right click inside editor and choose "Compile '<file name>'"

In order to run an application a corresponding type of run configuration need to be created.

1. Select Run->Edit configurations...
2. Click on "+" sign to add a new configuration
3. Choose "FPC executable"
4. Click "OK"

To specify a main project file which should be runned:
1. Select File->Project Structure
2. Choose a Pascal module
3. Adjust "Main file" setting on "Settings" tab

Now you can run a Pascal Module using Run command
