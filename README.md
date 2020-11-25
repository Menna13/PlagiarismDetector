# *Plagiraism Detector*

**Plagiraism Detector** is an android app that allows a user to browse a document from their phone and check for plagiarism/origniality through finding matching 
resources with the text in the document. The app utilizes [Google Custom Search JSON API](https://developers.google.com/custom-search/v1/overview).
This application was built to help teachers in Egypt STEM Schools support students in their semester-long Capstone project through giving feedback on their project posters and portfolios. Through utilizing this app, teachers can check for originality of student ideas and check for resources more efficiently. 

## Features 

The following functionalities are completed:

- [X] User can **Browse document from their internal, sdcard, or cloud storage**
- [X]	User can **view extracted text from the document**
- [X] User can input keywords for the text to integrate in the plagiarism search process
- [X] User can see a list of resources that are possible match for the content of the document

## How it works

The application is given permission to access device internal and external storage. Once the user chooses the document text file, the text is extracted and tokenized to sentences. The user is then allowed to optionally enter keywords that relate to the text and will be integrated in the search process. Finally, [Google Custom Search JSON API](https://developers.google.com/custom-search/v1/overview) is used to fetch and return the top matching resources to the text extracted.


## Motive
This project was developed to support the capstone project in my highschool. Students are required to work on semester-long capstone research project in which 
they propse solutions to grand challanges facing the counrty (home origin is Egypt). In order to support teachers and evaluatoers with grading and 
plagiarism/originality detection, this app was developed to help teachers guide students to original and concise research portfolios and posters. 


