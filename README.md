Prodigy
===
[![Project Status: Active – The project has reached a stable, usable state and is being actively developed.](https://www.repostatus.org/badges/latest/active.svg)](https://www.repostatus.org/#active)
**Disclaimer: this software is not reviewed or endorsed by any organization, and it is provided "as is" without any warranty of any kind. Any use is at your own risk!**

Prodigy is an open-source [Chaos Engineering](http://principlesofchaos.org/) experiment framework which provides control plane stack, CLI and SDK for AWS Java applications. You can easily build, deploy and monitor your own faults with Prodigy to simulate any scale of failures on your application.

Getting Started
---
### Prerequisites
* Git 2.x
* JDK 1.8+
* Apache Maven 3.x
* Python 3.x
* AWS Account
* AWS CLI with credentials for IAM user who has 'AdministratorAccess' permission
  - https://docs.aws.amazon.com/cli/latest/userguide/install-cliv2.html
  - https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-configure.html
* AWS SAM CLI for example application
  - https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/serverless-sam-cli-install.html (you can skip Docker installation for this Getting Started guide)

### Cloning repository
Open a terminal and navigate to the directory where you would like the clone to be stored and type the following command.

```
$ git clone https://github.com/eiichiro/prodigy.git
```

### Building Prodigy
Move to `prodigy` directory and type the following command to build Prodigy. Assume the current directory as `<PRODIGY_HOME>` from here.

```
$ mvn install
```

### Deploying Prodigy control plane stack
Type the following command to start Prodigy CLI.

```
$ <PRODIGY_HOME>/prodigy-cli/target/bin/prodigy
```

You can see Prodigy CLI prompt like this.

```
__________                   .___.__
\______   \_______  ____   __| _/|__| ____ ___.__.
 |     ___/\_  __ \/  _ \ / __ | |  |/ ___<   |  |
 |    |     |  | \(  <_> ) /_/ | |  / /_/  >___  |
 |____|     |__|   \____/\____ | |__\___  // ____|
                              \/   /_____/ \/      vx.y.z

Chaos Engineering experiment for AWS Java applications

Welcome to Prodigy. Hit the TAB or press 'hint' to display available commands.
Press 'help <command>' to display the detailed information for the command.
Press 'exit' to exit this session.
```

Prodigy CLI deploys separate control plane stack by the specified profile. Type the following command to deploy a new control plane stack with profile `prod`.

```
prodigy> deploy prod
```

The control plane stack is deployed in the region specified in the current AWS CLI configuration. The default is `us-east-1`.

### Injecting fault
There is an example application named 'Sushi' for the experience of a simple failure injection with Prodigy. Open another terminal session (note to keep current Prodigy CLI session) and move to the application directory by typing the following command.

```
(Another terminal session)
$ cd <PRODIGY_HOME>/prodigy-examples/sushi
```

First, you need to make a new Amazon S3 bucket for the application deployment by typing the following command.

```
$ aws s3 mb s3://<YOUR_BUCKET> --region <YOUR_REGION>
```

The bucket name can be arbitrary unless it has been already made. AWS region specified by `--region` option must be the same as where the control plane stack is deployed in (the default is `us-east-1`).

Type the following command to deploy Sushi application.

```
$ mvn deploy -Ds3Bucket=<YOUR_BUCKET> -DawsRegion=<YOUR_REGION> -DprodigyProfile=prod
```

The terminal shows the deployment result like this.

```
CloudFormation outputs from deployed stack
--------------------------------------------------------------------------------
Outputs
--------------------------------------------------------------------------------
Key                 Endpoint
Description         Prodigy Sushi API endpoint URL
Value               <PRODIGY_SUSHI_ENDPOINT>
--------------------------------------------------------------------------------
```

Note `<PRODIGY_SUSHI_ENDPOINT>` shown and type the following command to configure the application.

```
$ export PRODIGY_SUSHI_ENDPOINT=<PRODIGY_SUSHI_ENDPOINT>
```

Run the following Python script to restock sushi items in the application.

```
$ python ./target/scripts/restock.py
```

And run the following Python script to start periodically placing orders.

```
$ python ./target/scripts/order.py
```

The terminal shows order logs like this.

```
2020-05-03 13:58:13,206 INFO     1 horse mackerel served
2020-05-03 13:58:14,787 INFO     1 salmon served
2020-05-03 13:58:16,428 INFO     1 horse mackerel served
2020-05-03 13:58:17,946 INFO     1 sea urchin served
2020-05-03 13:58:19,665 INFO     1 sea urchin served
2020-05-03 13:58:21,205 INFO     1 medium fatty tuna served
2020-05-03 13:58:22,466 INFO     1 salmon roe served
2020-05-03 13:58:24,107 INFO     1 yellowtail served
2020-05-03 13:58:25,444 INFO     1 yellowtail served
2020-05-03 13:58:27,091 INFO     squid is out of stock
```

Let's inject a failure. Switch back to the Prodigy CLI session and type the following command.

```
prodigy|prod> inject dynamodb-error {"statusCode" : 503}
```

`dynamodb-error` is a ready-made fault that intercepts all accesses to Amazon DynamoDB and throws an exception with the specified HTTP status code. `order.py` script will print some errors.

Type the following command to check the injected fault status.

```
prodigy|prod> status
```

You can find `dynamodb-error` is in `ACTIVE` state.

```
name           id       status
-------------- -------- --------
dynamodb-error <FAULT_ID> ACTIVE
```

Type the following command to eject the fault previously injected.

```
prodigy|prod> eject <FAULT_ID>
```

Type the following command to check the injected fault status again.

```
prodigy|prod> status
```

The session will show the status like this.

```
name           id       status
-------------- -------- --------
dynamodb-error <FAULT_ID> INACTIVE
```

Check the state again and make sure it has been in `INACTIVE`.

Now you can exit both terminal sessions for this Getting Started.

Next Step
---
* CLI
* Build Your Own Faults
* Ready-made Faults
* Architecture
