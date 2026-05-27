// package com.tourly.auth.mapper;

// import com.tourly.auth.dto.response.UserResponse;
// import com.tourly.auth.entity.User;

// public class UserMapper {

//     public static UserResponse toResponse(User user) {
//         UserResponse res = new UserResponse();
//         res.setId(user.getId());
//         res.setFullName(user.getFullName());
//         res.setEmail(user.getEmail());
//         res.setPhone(user.getPhone());
//         res.setAccountStatus(user.getAccountStatus().name());
//         res.setEmailVerified(user.getEmailVerified());
//         res.setPhoneVerified(user.getPhoneVerified());
//         res.setKycVerified(user.getKycVerified());
//         res.setRoleName(user.getRole().getName().name());
//         return res;
//     }
// }

package com.tourly.auth.mapper;

import com.tourly.auth.dto.response.UserResponse;
import com.tourly.auth.entity.User;

public class UserMapper {

    public static UserResponse toResponse(User user) {
        UserResponse res = new UserResponse();
        res.setId(user.getId());
        res.setFullName(user.getFullName());
        res.setEmail(user.getEmail());
        res.setPhone(user.getPhone());
        res.setAccountStatus(user.getAccountStatus().name());
        res.setEmailVerified(user.getEmailVerified());
        res.setPhoneVerified(user.getPhoneVerified());
        res.setKycVerified(user.getKycVerified());

        if (user.getRole() != null && user.getRole().getName() != null) {
            res.setRoleName(user.getRole().getName().name());
        } else {
            res.setRoleName(null);
        }

        return res;
    }
}