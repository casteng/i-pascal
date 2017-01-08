Intellij IDEA Object Pascal plugin
==================================

Description
-----------
A free Object Pascal language plugin for excellent `IntelliJ IDEA <http://www.jetbrains.com/idea>`_ is to be released in May 2013.

Supports integration with Delphi compiler (dcc32) and `Free Pascal <http://www.freepascal.org>`_ compilers.<br />

Compatible with IDEA Community Edition as well as Ultimate Edition from version 13.xx and above.

Pascal-specific features currently implemented
++++++++++++++++++++++++++++++++++++++++++++++

* context-aware completion
* statements completion
* compiler defines and directives completion
* undeclared identifiers highlight
* add variable declaration quick fix
* missing method declaration/implementation highlight and quick fixes
* used units usage check and quick fixes
* missing routine implementation highlight and quick fixes
* override inherited methods
* navigate to super methods/classes
* navigate to implementations/descendants
* routine parameters hint
* DCU files interface view - DCU32INT (http://hmelnov.icc.ru/DCU) required
* PPU files interface view - FPC 2.7.x or above required
* navigation between unit routine interface definition and implementation
* navigation between class method definition and implementation
* rename refactoring
* braces matcher
* code commenter
* Free Pascal compiler integration
* Delphi compiler integration
* full Object Pascal parser
* conditional compilation support
* identifier declaration, references and usages search and highlight
* program running from the IDE
* syntax errors highlighting
* Object Pascal syntax highlighting
* folding support

As well as other features provided by IDEA
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

* full-text search with regular expressions
* version control systems integration (SVN, Git, Mercurial, ...)
* other languages support, such as XML and SQL
* spell checking
* TODO support

to name a few.

Planned in future releases
++++++++++++++++++++++++++

* code analysis
* full error insight with quick fixes
* probably bad code warnings with quick fixes
* refactoring
* import of Lazarus and Delphi projects
* extended includes support
* test frameworks support
* FPC for JVM support

Installation
------------

1. Select File->Settings->Plugins
2. Click install plugin from disk
3. Choose `IdeaPas.zip`

Creating new project
--------------------

**In order to compile Pascal applications a corresponding type of SDK need to be created in IDEA.**

1. Select File->New project
2. Select "Pascal Module"
3. Click "New" to the right of "Project SDK" box
4. Choose directory where FPC compiler is installed

**To adjust SDK settings:**

1. Select File->Project Structure
2. Choose SDKs
3. Click on desired SDK

Compiling and running
---------------------

To compile current file right click inside editor and choose "Compile '<file name>'"

**In order to run an application a corresponding type of run configuration need to be created.**

1. Select Run->Edit configurations...
2. Click on "+" sign to add a new configuration
3. Choose "FPC executable"
4. Click "OK"

**To specify a main project file which should be runned:**

1. Select File->Project Structure
2. Choose a Pascal module
3. Adjust "Main file" setting on "Settings" tab

Now you can run a Pascal Module using Run command
