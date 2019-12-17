# README #

## Project Name ##
Phoenix Finance README.md

## Developers ##
Craig
Mulalo
Phomolo
Celokushe

## Business Rules ##

### Complex loan charges ###

#### Transfer duty ####
|Value of the property (R)| Rate|
|--------------|----------------|
|0 - R900,000|0%|
|R900,001 - R1,250,000|3% of the value above R900,000| 
|R1,250,001 – R1 750 000|R10 500 + 6% of the value above R 1 250 000
|R1,250,001 - R1,750,000|R10,500 + 6% of the value above R1,250,000|
|R1,750,001 – R2,250,000|R40,500 + 8% of the value above R1,750,000|
|R2,250,001 – R10,000,000|R80,500 +11% of the value above R2,250,000|
|R10,000,001 and above|R933,000 + 13% of the value above R10,000,000|

#### Legal Fees ####
The biggest value of either; 0.8% of your bond or R15,000.00

#### Agent Fees ####
5%-7% of the bond

#### Bond Registration Fees ####
R5000 (fixed)

## Build ##

To be completed.

## Source Management ##

To be completed.

### Git ###

#### Commands ####

##### Branch management  #####

Creates a new branch from using the current branch as base
> git checkout -b <new-branch-name>

Changes from the specified branch to the
> git checkout <branch-to-change-to>

Merges the current branch with the provided branch
> git merge <branch-to-be-merged-to-the-current-branch>

##### Viewing Options #####

Show available __local__ branches
> git branch

Similar to show but defaults the difference between HEAD and the staged changes so far
> git diff

Show the recent changes to the branch(as well as affected snippets) 
> git show

Show staged, unstaged & untracked changes so far
> git status

##### Staging Files #####

Add__ a file to be staged
> git add <file>

Add all files from __root__ onwards recursively
> git add .

#### Unstaging Files ####

Stage a __deleted__ file
> git rm --cached	<file>

Stage deleted files
> git rm --cached -r <files>

Discard any changes on the current file and defaults to the last commit of that branch (HEAD)
> git reset HEAD <file>

Discard any changes on the current file and defaults to the last commit of the specified branch
> git reset <commit-hashcode> <file>

##### Commiting changes #####

Commit a single file's changes with a message
> git commit -m "<message>"

Commit all staged files to a single commit with a message
> git commit -am "<message>"

##### Staging Management #####

Temporarily store the current branch's staged changes
> git stash

##### Local Repo Management #####

Pull to overwrite current branch with origin/master(remote)'s latest commits
> git pull --rebase

Merge local changes to whilst preparing to handle confilcts
> git pull

Push the changes of the current branch to the applicable remote branch
> git push

##### Rebase conflicts #####

"Skips the patch"(Untested)
> git rebase --skip

Conflicts have been resolved, added& commited to the merge branch.
> git rebase --continue

Cancels the attempted rebase & checkouts to the original branch
> git rebase --abort

## Hacks ##

### Dev Methodology ###
+ Agile Manifesto
	* Read it
	* Find out who defined it
	* Why it was created 

+ Agile Breakdown
	* Ceremonies
		Stand Up - 15min
			Ask 3 questions
				> What did I do?
				> What am I do today?
				> What is blocking me from progressing?

	* Sprint
		5 days
			Has a sprint backlog
			Contains Tickets
			Can only hold one ticket at a time
			Concludes in a retro

	* Plan
		Has a product backlog
		Never work on anything that your product owner doens't approve or isn't in your product backlog.

	* Environments
		Development on a local machine->Quality Assurance->Production

		* Read Up
			On estimation points
			How we predict time for how stories are completed
				
+ Team Coding standards
	Pair Programming

### Curl ###

#### Resources ####

[Command Overview for POST](https://gist.github.com/subfuzion/08c5d85437d5d4f00e58)

[POST examples](https://gist.github.com/joyrexus/524c7e811e4abf9afe56)

#### Flags ####
|Command Flag|Description 	|
|------------|--------------|
| `-d`       | Data         |
| `-H`       | Header       |
| `-X`       | Request      |
| `-F`         | Form         |
|`-u <user:password>` |Login Details |

#### Command Examples ####

> curl -H "Content-Type: application/json" -X POST -d {\"name\":\"Command\",\"surname\":\"Line Utility\",\"dob\":\"1990-04-28\"} http://localhost:8080/jee_homework_hw6_b_version3_cdi/customer/add

> curl -X POST -F 'name=Command' -F 'surname=Utility' -F 'dob=1990-04-28' http://localhost:8080/jee_homework_hw6_b_version3_cdi/customer/add


> curl -H 'Content-Type: application/json' -X POST -d '{"name":"Command","surname":"Line Utility","dob":"1990-04-28"}' http://localhost:8080/jee_homework_hw6_b_version3_cdi/customer/add

##### Works for POST #####
Note Content-Type used by JBOSS i.e. application/x-www-form-urlencoded
>	curl -d "name=Hai&surname=Man&dob=2017-06-29" -X POST -H "Content-Type: application/x-www-form-urlencoded" http://localhost:8080/jee_homework_hw6_b_version3_cdi/customer/add
