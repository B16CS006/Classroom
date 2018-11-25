
'use strict';

const functions = require('firebase-functions');
const mkdirp = require('mkdirp-promise');
const admin = require('firebase-admin');
const spawn = require('child-process-promise').spawn;
const path = require('path');
const os = require('os');
const fs = require('fs');

admin.initializeApp();


exports.join_class_request = functions.database.ref('Join-Class-Request/{classId}/{userId}').onWrite((snapshot,context) =>{

	const userId = context.params.userId;
	const classId = context.params.classId;
	
	const as = snapshot.after.val().as;
	var rollNumber = snapshot.after.val().rollNumber;
	const request = snapshot.after.val().request;
	const name = snapshot.after.val().name;

	if(request == null || as == null){
		conslole.log('requst or as is null');
		return false;
	}

	if(request == 'pending'){
		console.log('Pending Request ');
		return false;
		//inform Teacher of the class
	}else if(request == 'reject'){
		console.log('Request is Rejected')
		return snapshot.after.ref.set(null);
	}else if(request == 'accept'){

		if(!(as == 'leave' || as == 'student' || as == 'teacher')){
			console.log('Argument is not valid : Type -> ', as);
			return snapshot.after.ref.set(null);
		}

		const classEnrollPath = `Class-Enroll/${userId}/${classId}`;
		const classroomPath = `Classroom/${classId}/members/${userId}`;

		if(as == 'teacher')
			rollNumber = null;

		var classroomValue = {
			'as':as,
			'rollNumber':rollNumber,
			'name':name
		}
		var classEnrollValue = {'as':as}

		if(as == "leave"){
			classroomValue = null;
			classEnrollValue = null;
		}

		return admin.database().ref(classroomPath).set(classroomValue).then((snapshot2) =>{
			console.log('Classroom/classId/members/userId value is changed');
			return admin.database().ref(classEnrollPath).set(classEnrollValue).then((snapshot3) => {
				console.log('Class-Enroll value is changed');
				return snapshot.after.ref.set(null);
			});
		});
	}
	return false;

});
/*
exports.join_class_request_conform = functions.database.ref('/Join-Class-Request/conform/{userId}/{classId}').onCreate((snapshot,context) =>{
	var type = snapshot.val();
	if(!(type == 'leave' || type == 'student' || type == 'teacher')){
		console.log('Argument is not valid : Type -> ', type);
		return snapshot.ref.set(null);
	}
	const userId = context.params.userId;
	const classId = context.params.classId;
	console.log('UserId : ',userId, ', ClassId : ', classId, ', Type : ',type);
	var classEnrollPath = `Class-Enroll/${userId}/${classId}/as`;
	var classroomPath = `Classroom/${classId}/members/${userId}/as`;
	if(type == "leave"){
		type = null;
		classEnrollPath = `Class-Enroll/${userId}/${classId}`;
		classroomPath = `Classroom/${classId}/members/${userId}`;
	}
	return admin.database().ref(classEnrollPath).set(type).then((snapshot2) =>{
		console.log('Class-Enroll value is changed');
		return admin.database().ref(classroomPath).set(type).then((snapshot3) => {
			console.log('Classroom/classId/members/userId value is changed');
			return snapshot.ref.set(null);
		});
	});
	
});
*/

exports.initializeStudentsMarks = functions.database.ref('/Classroom/${classId}/Assignment/{assignment}').onCreate( (snapshot,context) => {
	console.log('initializeStudentMarks');
	const classId = context.params.classId;
	const assignment = context.params.assignment;
	const title = snapshot.val().title;
	console.log('ClassId : ', classId, ', Assignment : ', assignment, ', Title : ', title);

	//Notify all Students
});

exports.generateThumbnail = functions.storage.object().onFinalize((object) => {

	// Max height and width of the thumbnail in pixels.
	const THUMB_MAX_HEIGHT = 200;
	const THUMB_MAX_WIDTH = 200;
	// Thumbnail prefix added to file names.
	const THUMB_PREFIX = 'thumb_';

  const filePath = object.name;
  const contentType = object.contentType; // This is the image MIME type
  const fileDir = path.dirname(filePath);
  const fileName = path.basename(filePath);
  const thumbFilePath = path.normalize(path.join(fileDir, `${THUMB_PREFIX}${fileName}`));
  const tempLocalFile = path.join(os.tmpdir(), filePath);
  const tempLocalDir = path.dirname(tempLocalFile);
  const tempLocalThumbFile = path.join(os.tmpdir(), thumbFilePath);

  if (!contentType.startsWith('image/')) {
    return console.log('This is not an image.');
  }

  if (fileName.startsWith(THUMB_PREFIX)) {
    return console.log('Already a Thumbnail.');
  }

  const bucket = admin.storage().bucket(object.bucket);
  const file = bucket.file(filePath);
  const thumbFile = bucket.file(thumbFilePath);
  const metadata = {
    contentType: contentType,
    // To enable Client-side caching you can set the Cache-Control headers here. Uncomment below.
    // 'Cache-Control': 'public,max-age=3600',
  };
  
  // Create the temp directory where the storage file will be downloaded.
  mkdirp(tempLocalDir)
  // Download file from bucket.
  file.download({destination: tempLocalFile});
  console.log('The file has been downloaded to', tempLocalFile);
  // Generate a thumbnail using ImageMagick.
  spawn('convert', [tempLocalFile, '-thumbnail', `${THUMB_MAX_WIDTH}x${THUMB_MAX_HEIGHT}>`, tempLocalThumbFile], {capture: ['stdout', 'stderr']});
  console.log('Thumbnail created at', tempLocalThumbFile);
  // Uploading the Thumbnail.
  bucket.upload(tempLocalThumbFile, {destination: thumbFilePath, metadata: metadata});
  console.log('Thumbnail uploaded to Storage at', thumbFilePath);
  // Once the image has been uploaded delete the local files to free up disk space.
  fs.unlinkSync(tempLocalFile);
  fs.unlinkSync(tempLocalThumbFile);
  // Get the Signed URLs for the thumbnail and original image.
  const config = {
    action: 'read',
    expires: '03-01-2500',
  };
  const results = Promise.all([
    thumbFile.getSignedUrl(config),
    file.getSignedUrl(config),
  ]);
  console.log('Got Signed URLs.');
  const thumbResult = results[0];
  const originalResult = results[1];
  const thumbFileUrl = thumbResult[0];
  const fileUrl = originalResult[0];
  // Add the URLs to the Database
  admin.database().ref('images').push({path: fileUrl, thumbnail: thumbFileUrl});
  return console.log('Thumbnail URLs saved to database.');
});
