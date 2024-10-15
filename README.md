# clean-up-gmail
Automatically delete promotional emails older than a month using Gmail API.

## Installation
1. Clone the repository:
```bash
 git clone https://github.com/Kiran-Grewal/clean-up-gmail.git
```
## Getting Started
To use clean-up-gmail, you'll need to download the OAuth Client ID credentials file from Google cloud console.

Follow the instructions in the 'Set up your environment' section at https://developers.google.com/gmail/api/quickstart/java#set-up-environment.
1. Enable the API
2. Configure the OAuth consent screen
3. Authorize credentials for a desktop application
   - Download the JSON file, rename it as credentials.json and move it to the working directory.
   
Note: The first time you run the program, a browser window will open, and you'll be asked to give permission to the application. This will save an access token and create a Tokens folder.

## Usage
Run the gmailCleaner.main( ) and All of your promotional emails older than a month would be moved to the trash folder.

![](https://github.com/Kiran-Grewal/clean-up-gmail/blob/main/screenshots/running%20gmailCleaner.main().png)


   
   
