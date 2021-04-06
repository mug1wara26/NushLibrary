const functions = require("firebase-functions");
const admin = require('firebase-admin');
const https = require('https');
const nodemailer = require('nodemailer');
admin.initializeApp();

let mailTransport = nodemailer.createTransport({
    service: 'gmail',
    auth: {
        user: functions.config().email.user,
        pass: functions.config().email.pass
    }
});

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//   functions.logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });

// Take the text parameter passed to this HTTP endpoint and insert it into
// Firestore under the path /messages/:documentId/original
exports.addMessage = functions.https.onRequest(async (req, res) => {
    // Grab the text parameter.
    const original = req.query.text;
    // Push the new message into Firestore using the Firebase Admin SDK.
    const writeResult = await admin.firestore().collection('messages').add({original: original});
    // Send back a message that we've successfully written the message
    res.json({result: `Message with ID: ${writeResult.id} added.`});
});

// Listens for new messages added to /messages/:documentId/original and creates an
// uppercase version of the message to /messages/:documentId/uppercase
exports.makeUppercase = functions.firestore.document('/messages/{documentId}')
    .onCreate((snap, context) => {
        // Grab the current value of what was written to Firestore.
        const original = snap.data().original;

        // Access the parameter `{documentId}` with `context.params`
        functions.logger.log('Uppercasing', context.params.documentId, original);

        const uppercase = original.toUpperCase();

        // You must return a Promise when performing asynchronous tasks inside a Functions such as
        // writing to Firestore.
        // Setting an 'uppercase' field in Firestore document returns a Promise.
        return snap.ref.set({uppercase}, {merge: true});
    });

exports.sendMail = functions.https.onCall((data, context) => {
    console(JSON.stringify(data))
    const recipientEmail = data['recipientEmail'];
    console.log('recipientEmail: ' + recipientEmail);
    const mailType = data['type']
    console.log('mailType: ' + mailType)

    const mailOptions = {
        from: 'Node <aloysiusgsq@gmail.com>',
        to: recipientEmail
    };

    if (mailType === "request") {
        mailOptions.subject = 'Your request to change username has been processed';
        const accepted = data['accepted']
        if (accepted === true) {
                mailOptions.html =
                    `<p style="font-size: 16px;">Request accepted</p>
                    ` // email content in HTML
        }
        else {
            mailOptions.html =
                `<p style="font-size: 16px;">Request denied</p>
                ` // email content in HTML
        }
    }

    return mailTransport.sendMail(mailOptions).then(() => {
        console.log('email sent to:', recipientEmail);
        return new Promise(((resolve, reject) => {
            return resolve({
                result: 'email sent to: ' + recipientEmail
            });
        }));
    });
});