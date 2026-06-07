UPDATE host_verifications hv
JOIN users u ON hv.user_id = u.user_id
SET hv.aadhaar_number = u.aadhaar_number,
    hv.pan_number = u.pan_number
WHERE u.aadhaar_number IS NOT NULL OR u.pan_number IS NOT NULL;
