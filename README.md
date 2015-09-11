----------
Compiling
----------
SBT is used to build the code. To compile, just run the following command

$ sbt compile

--------
Running 
--------

SBT is used to run the code. To run, just run the following command

$ sbt
> run 3

where 3 is the number of leading zeros required

----------------------
Command Line Arguments
----------------------

The code expects a non-negative integer less than 256 as a command line argument.
This denotes the number of leading zeros required in the bitcoin.




-------------
Architecture
-------------


					      |===================|
				              |  Result Listener  |
					      |===================|
							^
							|
							|
					  Output(bitcoinStr, numWorkers)
						        |
							|
							|
|=========================|			 |============|--------Mine(numZeros)----->|==============|
|     BitcoinMining       |--------Start(k)----->|   Master   |				   |    Worker    |  
|=========================|			 |============|<------Result(bitcoin)------|==============|

   


----------------------
Size of the work unit
----------------------




--------------------------
Result for 4 leading zeros
--------------------------




------------------------------------------
Running Time when run for 5 leading zeros
------------------------------------------




-------------------
Coins with most 0s
-------------------


----------------------------------------
Largest Number of Working Machines Used
----------------------------------------




