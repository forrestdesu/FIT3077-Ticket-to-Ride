# FIT3077 Repository - Sprint 2
- All the code from all the teammates is if the prototypes folder.
- Each folder inside the prototype folder has name of the teammate the code belongs to.
- Each individual folder has its own .jar file that can either be copied to Desktop and double click to open, or just go to the folder it is saved in and double click to run it.
- If the file does not open or gives an Java extension error:
  - On Mac - please go to the folder that has the .jar file and right click that folder and choose to open terminal at that folder and write "java -jar [jar file name].jar"
  - On Windows - please go inside the folder that has the .jar file and right click anywhere in the folder and choose to open terminal in that folder and write "java -jar [jar file name].jar"

# Main.java is the file that runs the Java code
# ---- Steps to make a JAR file ----

Step 1: Open your project in IntelliJ and click "File" on the top menu, then choose "Project Structure"

Step 2: In the "Project Structure" window, click on "Artifacts" on the left side

Step 3: Click the " + " button, select "JAR" and choose "From modules with dependencies"

Step 4: Select the main class of your program, name your JAR file, then click OK.

Step 5: Press "Apply", then click OK on "Project Structure" window.

Step 6: Go to Build menu at the top of IntelliJ and click "Build Artifacts".

Step 7: Select the JAR file you just created and choose "Build"

Step 8: After its built, IntelliJ should generate the JAR file in the "out" folder

Step 9: Find the JAR file in that folder, then copy it to desktop or double-click it to test if it runs properly.

# ---- Steps to fix issue where code cannot be run as all the java files are not working ----
Step 1: Open your project in IntelliJ and click "File" on the top menu, then choose "Project Structure"

Step 2: In the "Project Structure" window, click on "Modules" on the left side

Step 3: Click the " + " button, select "Import Module" and choose the src folder that has all the code.

Step 4: After the keep pressing next until you reach the final screen and click "Create"

Step 5: Click "Apply" and then "Ok" on the previous "Project Structure" window

Step 6: Go to Main.java file and run it, and if any other pop up comes about "Edit Configuration", just click "Run" and then "Run Anyways" on the next pop up.

