const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);


exports.sendNotification = functions.database.ref('/notifications/{user_id}/{notification_id}').onWrite((change, context) => {

	const user_id = context.params.user_id;
	const notification_id = context.params.notification_id;
	console.log('user_id: ', user_id);
	console.log('notification_id', notification_id);

	if (!change.after.val()) {
		console.log('A notification has been deleted from database');
		return 0;
	}

	const fromUser = admin.database().ref('/notifications/' + user_id + '/' + notification_id).once('value');
	return fromUser.then(function(snapshot) {
		const from_user_id = snapshot.val().from;
		console.log('A new notification from from_user_id: ', from_user_id);

		const userQuery = admin.database().ref('/Users/' + from_user_id + '/name').once('value');
		const deviceToken = admin.database().ref('/Users/' + user_id + '/device_token').once('value');

		return Promise.all([userQuery, deviceToken]).then(function(snapshot) {
			const userName = snapshot[0].val();
			const token_id = snapshot[1].val();
			console.log('userName: ', userName);
			console.log('token_id: ', token_id);

			const payload = {
					notification: {
						title: "Friend Request",
						body: userName + " has sent a Friend Request to you",
						icon: "default",
						click_action: "com.yt.chat_TARGET_NOTIFICATION"
					},
					data: {
						from_user_id: from_user_id
					}
				};

				return admin.messaging().sendToDevice(token_id, payload)
				.then(response => {
					console.log('This was the notification feature');
					console.log('response: ', response);
				})
				.catch(error => {
					console.log('error: ', error);
				})

		}); // END Promose.all
		
	}); // END fromUser



	


});