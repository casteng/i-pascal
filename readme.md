Intellij IDEA Object Pascal plugin
==================================

Description
-----------
Project homepage: http://www.siberika.com/ipascal.htm

A free Object Pascal language plugin for excellent IntelliJ IDEA

Supports integration with Delphi compiler (dcc32) and Free Pascal compilers.

Compatible with IDEA Community Edition as well as Ultimate Edition from version 13.xx and above.

Pascal-specific features currently implemented
----------------------------------------------

* run and debug right in the IDE
* GDB and LLDB debug backends
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
* syntax errors highlighting
* Object Pascal syntax highlighting
* folding support

As well as other features provided by IDEA
------------------------------------------

* full-text search with regular expressions
* version control systems integration (SVN, Git, Mercurial, ...)
* other languages support, such as XML and SQL
* spell checking
* TODO support

to name a few.

Planned in future releases
--------------------------

* code analysis
* full error insight with quick fixes
* probably bad code warnings with quick fixes
* refactoring
* import of Lazarus and Delphi projects
* extended includes support
* test frameworks support
* FPC for JVM support

Developer environment configuration
-----------------------------------
Instructions
https://www.jetbrains.com/help/idea/2016.3/plugin-development-guidelines.html

Lexer and parser classes are generated with [Grammar Kit plugin](https://plugins.jetbrains.com/plugin/6606-grammar-kit).

To generate _PascalLexer class open pascal.flex, right click and choose "Rin JFlex generator".

To generate parser classes open pascal.bnf, right click and choose "Generate parser code".

**There are JUnit tests. Please ensure the tests passing before creating pull requests.**

Other information
-----------------
Information on how to install and use the plugin is available at `I-Pascal homepage <http://www.siberika.com/ipascal.htm>`_.