# Deployment on AWS

## Make necessary Preparations
```
cd ~
sudo apt-get install maven
mkdir jboss
cd jboss
git clone https://github.com/jboss-outreach/facebook-echo-bot
cd facebook-echo-bot
mvn install
mvn clean package
```

## Get Facebook Access Token

1. Create a new page on Facebook
2. Create a new App
3. Select the page and generate Access_token and copy it for later use.

## Run the server
Type the following code to run the server
```
java -Dhttp.address=0.0.0.0 -Dhttp.port=9090 -Dfacebook.access.token=[Access_token] -Dfacebook.verify.token=[Random] -jar ./target/facebook-echo-bot-3.5.0-jar-with-dependencies.jar
```

## Forward secure tunnel

1. Register at https://ngrok.com
2. Download and configure it.
```
wget https://bin.equinox.io/c/4VmDzA7iaHb/ngrok-stable-linux-amd64.zip
unzip ngrok-stable-linux-amd64.zip
./ngrok [auth_token at get started page]
```
3. Forward Port 9090
```
./ngrok http 9090
```

## Configure webhooks

1. On Facebook App Page, Click on configure webhooks.
2. Let the ngrok website you'll get at dashboard be ngrok_url
3. In Callback Url Type [ngrok_url]/webhook
4. In verify token, use the same as used earlier [Random]
5. Check the messages event.
6. Click on Verify and Save.

## Testing

1. Visit your Page.
2. Change to Viewer's Mode.
3. Type something like "Hi", "Jokes Please" etc.
4. Get a response.
