# GitHub-REST-API-Test-Project
 This project targets to use the API offered by GitHub to list 10 most conmitters of 5 different repositories. 
 
 ## Author
 - Hüveyscan Kamar
 
 ## Description
 
 In this project, I have tried to get 10 most committers of 5 different repositories by using GitHub's API and Java Programming Language. 
 
 Disclaimer: Final test couldn't have been finished due to rate limits of the API.
 
 ## How does it work?
 
 1- The program goes through all the commits of target repository and puts each committer in a set (to avoid dublicates).
 2- If the committer is not added to the set yet, it is being added while also creating an object with which holds its username and commit count (1 default)
 3- If the committer is already added to the set, the commmit count of its related object is increased.
 4- After finishing this process, all the committers are sorted among each other according to their commit counts. 
 5- Then, 10 most committers (if number of committers less than 10, then all of them) are written in to a text file with their username, location and company information. 
 6- Since all the necessary tests haven't been made, there might have been some bugs incase of a null returning. Be aware.
 7- I have created a Flask Server and cached some of the JSON responses of the GitHub API to be able to do at least some tests. 
 8- During tests, I have used a localhost which I have implemented by using Flask. The URL line of it can be found under the source files (commented-out)