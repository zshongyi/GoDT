# GoDT - Eclipse Golang Development Tooling
GoDT is [Eclipse](https://www.eclipse.org/) plugin that helps with writing golang code inside [Eclipse](https://www.eclipse.org/) editor.  
It greatly facilitates writing of golang code with help of [gopls (the Go language server)](https://github.com/golang/tools/tree/master/gopls).

Implemented features:
* Code syntax highlighting
* Automatic completion
* Go to definition
* Other LSP support features
* build
* go mod commands
* go project neature

Next in development plan:
* Code debugging

# Screenshots
[mainwindow]: https://github.com/svujic/GoDT/blob/dev/web/screens/mainwindow.png "Eclipse Main Window"
[popuphelp]: https://github.com/svujic/GoDT/blob/dev/web/screens/popuphelp.png "Popup Help"
[completion]: https://github.com/svujic/GoDT/blob/dev/web/screens/completion.png "Completion Help"
[errorcorr]: https://github.com/svujic/GoDT/blob/dev/web/screens/errorcorr.png "Error Corection"
![alt text][mainwindow]![alt text][popuphelp]![alt text][completion]![alt text][errorcorr]

# Installation
Prerequisites:  
* installed Eclipse
* installed golang (go) and PATH environment variable points to it

If you want to download Eclipse -> [https://www.eclipse.org/downloads/](https://www.eclipse.org/downloads/)  
If you want to download golang -> [https://go.dev/](https://go.dev/)

This section will describe **three (3)** methods of installation.
1. method - Using **Eclipse Marketplace Client** inside Eclipse

   Start 'Eclipse Marketplace' with going to main menu: Help->Eclipse Marketplace  
   locate **find** entry box and write golang in it and then press button Go  
   after search finishes somewhere in list you will find GoDT and follow procedure afterwards  

2. method - Using web (browser)

   Go to [https://marketplace.eclipse.org/content/eclipse-golang-development-tooling](https://marketplace.eclipse.org/content/eclipse-golang-development-tooling)  
   then follow instruction on the page

3. method - Using **Install New Software** inside Eclipse

   follow instruction on [how to add new software repository to Eclipse](https://marketplace.eclipse.org/content/eclipse-golang-development-tooling/help)  
   on second step under **Location** enter: `https://zshongyi.github.io/GoDT/update/site.xml`  
   after that just proceed with remaining steps


   
