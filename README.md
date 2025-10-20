# Modern Pictochat

#### Pictochat in your browser, but without the limitations of being a recreation

[_Website Repo_](https://github.com/xFN10x/ModernPictochat-Website)

Modern Pictochat is website with the same premise of pictochat; chat pictorally!

## Planned Developments

_These features will be added if i continue to work on this_

- Custom Chat Icons
- Normal Text Chatting
- Better UI
- More Chats

## Building

Modern Pictochat comes in 2 distinct parts; server, and website. However, you only need to look at this repo for the info.

_The server is made with Java 21 using a Jetty server._

_The website is made in Javascript and html._

### Step 1: Build Server

What you are going to need to do before anything else is building the java program.

First, download the source code and extract it.
![Download source code](/readmeimages/download.png)

Now, you are going to open a terminal in the same folder as the `gradlew.bat` and `gradlew` files.

Now run:

```bat
gradlew shadowjar
```

After it is done, you should be able to find the jar in `/build/builtJars/`

### Step 2: Setup website, and SSL

**If you dont have any SSL certificates, get them now. It is required to run the server.**

Now, if you have decided to run the jar, you should get a prompt to type a directory. We are going to make that directory now.

So, make the directory somewhere on your drive, like C:/ or /var/opt/.

Now, you are going to want to go into that directory (with a CLI), and run

```bat
git clone https://github.com/xFN10x/ModernPictochat-Website.git
```

This should make a new folder inside called: `ModernPictochat-Website`. Rename that to `website`.
_(If you want to update the website, simply go into the `website` folder, and run `git pull`.)_

Now, you will need SSL certificates.

_(Ill assume you have a public, private, and domain certificate.)_

[This is a guide for how to make a JKS](https://www.baeldung.com/convert-pem-to-jks#3-pkcs12-to-jks) (Java KeyStore), which is required for this

Ill assume you have that JKS now, so move it to the directory with the website folder, and name it `cert.jks`

Finally, we are going to setup HCaptcha.

[You are going to need to setup HCaptcha, and get the secret key, and public key.](https://docs.hcaptcha.com/)

Now that you have those, create a new file along side those `website` and `cert.jks` files/folders, and call it: `secretCapchaKey.txt`

Now in this txt, simply put in the secret key, with no whitespaces, and save.

Now go to the website folder, go to `index.html`, go to line 69 (haha funny number... moving on,) and it should be on an element that looks like this:

```html
<h-captcha
        id="signupCaptcha"
        site-key="93d0a366-6982-4558-a108-62a03b96ce24"
        host="mpc.xplate.dev"
        size="normal"
        theme="dark"
        tabindex="0"
></h-captcha>
```

Replace the `site-key` with your site key,
And also replace host with the domain attributed to the site key and save.

(Your file structure should look like this)
![file structure](/readmeimages/structure.png)

#### Now you should be able to start the server, and have it up and running
