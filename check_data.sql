SELECT host_verification_id, aadhaar_number, pan_number FROM host_verifications;
SELECT user_id, aadhaar_number, pan_number FROM users WHERE aadhaar_number IS NOT NULL;
