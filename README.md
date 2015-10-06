# Team
Rahul Prabhu

Sanil Sinai Borkar


# Compiling
SBT is used to build the code. To compile, just run the following command from the project root direcory
```
$ sbt compile
```


# Running 

SBT is used to run the code. To run as a master, just run the following command
```
$ sbt
> run <number_of_leading_zeros>
```

To run as a client (worker), just run the following command
```
$ sbt
> run <server_ip_address>
```


# Command Line Arguments


When acting as a server, the code expects a non-negative integer less than 256 as a command line argument. This denotes the number of leading zeros required in the bitcoin.
```
> run <number_of_leading_zeros>
```

When acting as a client, the code expects an IP address of the server where the master is running.
```
> run <server_ip_address>
```



# Architecture

```
        									|===================|													|==============|
											|  Result Listener  |													| 	SHAWorker  |							|===================|													|==============|
														^																^		|
														|																|		|
														|																|		|
					Output(bitcoin, totalBitcoins, totalStrings, timeTaken, timeForWorkers)				Verify(randomString,   SHAResult(found, bcString,
														|														numZeros)			timeTaken)
														|																|		|	
														|																|		v
|=========================|						|============|<---------------------RequestWork---------------------|==============|
|     BitcoinMining       |------Start(ip)----->|   Master   |--------------------Mine(numZeros)------------------->|    Worker    |  
|=========================|						|============|<----Result(bitcoinStr, totalStrings, timeTaken)------|==============|
```

The *application.conf* requires the IP address and the port of the system on which it will listen for incoming (worker) connections. In this case, the system will act as the master for the incoming worker connections.

When running the program, the gator ID '*rprabhu*' was used as a prefix for generating strings.



# Size of the work unit

We carried out various experiments, and found the below mentioned work unit size to be the best one. This was achieved by a total of 100 actors.

| Work Unit Size | Real Time (ms) | CPU Time (ms) |	Parallelism Achieved |
|----------------|--------------------------------|----------------------|
|	429248	| 8572		|		52610			|	6.137424172 |
|	574100	| 10618 		|		73232			|	6.896967414 |
|   13284961	|	 207619		|	   1122734			|	5.407665002 |



# Result for 4 leading zeros
The code was run on 8-core machine. The results for finding 4 leading zeros when the master was run on this machine are given below. Time was measured in milliseconds.
```
rprabhu@XPS:~/Coding/bitcoinmining$ sbt
Picked up JAVA_TOOL_OPTIONS: -javaagent:/usr/share/java/jayatanaag.jar 
[info] Set current project to My Project (in build file:/home/rprabhu/Coding/bitcoinmining/)
> run 4
[info] Running BitcoinMining 4
[INFO] [09/13/2015 22:47:51.014] [run-main-0] [Remoting] Starting remoting
[INFO] [09/13/2015 22:47:51.251] [run-main-0] [Remoting] Remoting started; listening on addresses :[akka.tcp://BitcoinMining@192.168.0.4:3000]
[INFO] [09/13/2015 22:47:51.255] [run-main-0] [Remoting] Remoting now listens on addresses: [akka.tcp://BitcoinMining@192.168.0.4:3000]
rprabhurc7VKsa3cZKTjAn	0000E4165AF0076BF96947BD9DA4EFA8D4CDCEFA57FACE7FC014D28ADFEFBE05
rprabhua03WFgt0kcYJmxY	0000313202C5C53FCE40C060055A8C94A73EA62FFE1DDAB70234CB53321E21DA
rprabhupalvxcMn3dzzBT0	00003542C7936840873B89EA77913B1BDDD5EFA668D195BB6714CE85F0AE5FF7
rprabhubBrc1SD7mHS0F12	0000B69AB1233E9A0D0856A44723B23BDA466DB28DF7A4D89C48FBF841257275
rprabhuoNS029Zs9iHGVam	00007B5167F042173D0C3852A1ABB0F1E84B116A10FD02FBE5EE17C917C76474
rprabhukZFTdhClW4Dzg9L	00009E211F66964A01F52C54D100C77B86343A84F2D7E42D763704BA362368E7
rprabhuhG8bUvuC0dCSFD6	0000DC5A56C4E6446F91A143C65ACA40C6F5F1F52B767606E1594E3FF4F3F608
rprabhulPWRVxp30sKDRnw	00009CF069D32E80D0C8F84EF4E4002AEC02FDF30270F7E8EC4E4E7E0A790813
rprabhuV4agNhtBuX1ghbd	00004BC71F21B94AC4F3AE3E14C10AD16EF3185A0252C509DD35DD3F2C1ECA00
rprabhuPFFhA1nn5KO73ZE	000061BF03193CC30C93114F6732CDC9E9DD95C4A475C4E9503DBB944B8F34AC
Work Units: 574100
Real time = 10618	CPU Time = 73232
Parallelism: 6.896967414
```



# Running Time when run for 5 leading zeros
The code was run on 8-core machine. The results for finding 4 leading zeros when the master was run on this machine are given below. Time was measured in milliseconds.
```
rprabhu@XPS:~/Coding/bitcoinmining$ sbt
Picked up JAVA_TOOL_OPTIONS: -javaagent:/usr/share/java/jayatanaag.jar 
[info] Set current project to My Project (in build file:/home/rprabhu/Coding/bitcoinmining/)
> run 5
[info] Running BitcoinMining 5
[INFO] [09/13/2015 22:43:58.474] [run-main-0] [Remoting] Starting remoting
[INFO] [09/13/2015 22:43:58.710] [run-main-0] [Remoting] Remoting started; listening on addresses :[akka.tcp://BitcoinMining@192.168.0.4:3000]
[INFO] [09/13/2015 22:43:58.713] [run-main-0] [Remoting] Remoting now listens on addresses: [akka.tcp://BitcoinMining@192.168.0.4:3000]
rprabhuv20Qhqx37CqCka3	00000AFCEC19F55F91487E56A1EB61A834371BE9B448B5A818D574284E4322C4
rprabhujW4fddQUokKJ5Gw	0000044E47B9C636CC4E186736D5C863F30561F7C331E39FCAF2AB4CD48CF6BE
rprabhu7JtU8Wfx6ILW5qR	000006742D992A92CC8F14C8FECCB8BFBA6AFF8034743FCF809CFDC5FDECC063
rprabhuCn5TwKGd4nMo5AM	00000B06AB1AD306C45CD7E73C664578C3CB27312F5CEBD183CD87B43A0BFD20
rprabhu0zfUUBvrFmJHVNm	00000F5CF0BB8799D2B2DE28633D3A5362D90913C52713F6DCA72F7D7099084E
rprabhuqjkiCyUlnbNLOFZ	000004AA3F4EA976490FAFDFE5B2C07FE7331AF56D67B4D280C8E6D1AA4A12FA
rprabhuKMS7qUuKBCZ6lrH	00000C3C4BE52DFB3074D8399EEF88038DF76CD251EDDEB9E9D8E845A56C4331
rprabhucsMUgx4RYMjSRZR	000003CBC5B27655B5C07481CBF3A0A28D46DB08AEB405D6245D427FF8DD6633
rprabhuXdj88oliV5dYNOI	00000ED387174BA8DF666E6BA8EC6870D2DFB7506BA62EF8DA258E07949B5F53
rprabhu7sgGeYgaiq9pkWP	00000A5FEE0D3918CB2AE7659F85EBD32ED7DB9C853913A9BFE189A2383B4CF2
Work Units: 13284961
Real time = 207619	CPU Time = 1122734
Parallelism: 5.407665002
```



# Coins with most 0s
Six
```
rprabhuUhy1kRPq9HbtjpT 00000055AE93BCF43426353C93C508F3ECE801BC012D9324BDDCF8CFEB01F903
rprabhuOZp83966eR57BBP 000000900A66493B749545610A9237AB1A7FFE390266F7F8F5B1DCE67E511B15
```

Seven
```
rprabhuNL5nGdxoG8X23m2	00000008FCBE037E17C6651B0BA6AFAC852C1B421F9E39C8EDA9B3945BD93972
```


# Largest Number of Working Machines Used
Three. The 8-core machine acted as the server.
