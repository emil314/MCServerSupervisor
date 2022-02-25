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
If you want to put your server in the same folder as the rest, you can just comment out the working directory like this:
```
#working_directory=
```
