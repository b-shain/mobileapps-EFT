mobileapps-EFT
==============

Encrypted File Transfer Application

This application will:
•	Create an encryption key that will be shared between two devices when they communicate via NFC
o	Each device will randomly generate a key and then over NFC they will exchange it with the other device, and then perform an operation on the keys to generate a new key that will be used by both devices to encrypt/decrypt.
o	The new key should be the same for both devices and never need to leave the application
o	Type of encryption is still TBD, need to look at Android’s Java APIs to see supported encryption algorithms 
•	Access files on the device and be able to encrypt them, and then send those files to other devices
•	Store paired device information and the key associated with the device in a list of accounts
•	Will communicate over local networks using a transfer protocol supported by Android’s API or through a library. (eg. FTP, SFTP)
