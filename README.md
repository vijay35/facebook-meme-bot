# Facebook echo bot using Vert.x
![Build Status](https://travis-ci.org/jboss-outreach/facebook-echo-bot.svg?branch=master)

This project shows how to deploy a facebook echo bot Vert.x 3 applications to Heroku. The same application can be deployed using 3 approaches:

* Using a one click badge
* Using the maven plugin
* Using the git interface

If you use a fat-jar then deploying on heroku is as simple as one click. The only requirement is to create the Heroku specific [Procfile](../Procfile) with a `Dyno` of type web.

## Setup

### Using Heroku Toolbelt

Follow these steps, after installing the [Heroku Toolbelt](https://toolbelt.heroku.com/), to deploy with Maven:

```sh-session
$ git clone https://github.com/jboss-outreach/facebook-echo-bot.git
$ heroku create
$ mvn package heroku:deploy
```

### On local environment

Follow these steps, after installing the [Heroku Toolbelt](https://toolbelt.heroku.com/), to deploy with Maven:

```sh-session
$ git clone https://github.com/jboss-outreach/facebook-echo-bot.git
$ mvn clean package 
$ java -jar target/facebook-echo-bot-3.5.0-jar-with-dependencies.jar
```

**With localhost you can use https://ngrok.com/ for setting up https tunnel**

```
$ ./ngrok http 8080
```

### Webhook

Webhook is at `<host>:<port>/webhook`

### Facebook App setup document

https://developers.facebook.com/docs/messenger-platform/getting-started/app-setup

### Env properties

You need to take care of 4 properties :
```
 facebook.verify.token    # you decide this string. Just neet to put it same at both app and facebook
 facebook.access.token    # used to send pi hits to facebook on your behalf.
 http.port                # it is 8080 by default
 http.address             # it is 0.0.0.0 by default
```

One of way to set is to `-D<prop-name>=<prop-val>`

eg : 
```
$ java -Dhttp.port=$PORT -jar target/facebook-echo-bot-3.5.0-jar-with-dependencies.jar
```

When creating a project of your own, you'll need to borrow from the [`Procfile`](https://github.com/jboss-outreach/facebook-echo-bot/blob/master/Procfile) in the root directory of this project, and the `MAVEN_CUSTOM_OPTS` will only be necessary if your app is not the primary module of your Maven project.
