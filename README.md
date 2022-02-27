# MCServerSupervisor
A Minecraft Server can demand many resources. To stop this, I wrote this simple Supervisor which provides an easy control over your Server via MQTT, assuming you already have a suitable broker.
## Installation
To install it, you have to download the latest .jar from the releases tab. Then you can create your on supervisor.config file as following:
```
broker_address=0.0.0.0:0000
broker_pass=password
broker_user=user
topic_command=servercmd
topic_control_server=servercontrol
topic_control_supervisor=syscontroll
sever_gui=false
working_directory=/full/path/to/directory
server_file=server.jar
```
The topic_command ist the topic to which you can send minecraft commands to control your server. <br \>
The topic_control_server is the topic to which you can send the payloads ```ON``` and ```OFF``` to start and stop your minecraft Server. When the Server chrashes, it will automatically restart.<br \>
The topic_control_supervisor is the topic to which you can send the payload ```OFF``` to shut down the Supervisor (I don't know when you need this).<br \>
If you want to put your server in the same folder as the rest, you can just comment out the working directory like this:
```
#working_directory=
```
## To-Do
Option for custom Java Commands.
