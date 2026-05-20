package com.javfxtutorial.hethongdaugia.server.manager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.javfxtutorial.hethongdaugia.common.Exception.auth.InvalidCredentialsException;
import com.javfxtutorial.hethongdaugia.common.model.User;
import com.javfxtutorial.hethongdaugia.common.model.enums.AccountType;
import com.javfxtutorial.hethongdaugia.server.dao.UserDAO;
import com.javfxtutorial.hethongdaugia.server.security.PasswordHasher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

@DisplayName("Luồng nghiệp vụ tài khoản người dùng")
class UserContractTest {

    private User alice() {
        return new User(
                1,
                "alice",
                "pass123",
                "alice@example.com",
                "0901234567",
                AccountType.USER,
                "alice.png");
    }

    @Nested
    @DisplayName("UserManager.authenticate")
    class AuthenticationManagerTest {
        @Test
        @DisplayName("đăng nhập thành công với mật khẩu plaintext cũ")
        void authenticate_returnsUserWhenLegacyPlainPasswordMatches() throws Exception {
            User user = alice();
            UserDAO userDAO = mock(UserDAO.class);
            when(userDAO.selectByUsername("alice")).thenReturn(user);
            when(userDAO.update(any(User.class))).thenReturn(1);

            try (MockedStatic<UserDAO> mockedUserDAO = mockStatic(UserDAO.class)) {
                mockedUserDAO.when(UserDAO::getInstance).thenReturn(userDAO);

                User result = UserManager.getInstance().authenticate("alice", "pass123");

                assertSame(user, result);
                assertEquals("alice", result.getName());
                verify(userDAO).selectByUsername("alice");
            }
        }

        @Test
        @DisplayName("sai đăng nhập khi không tìm thấy user")
        void authenticate_throwsInvalidCredentialsWhenUserMissing() throws Exception {
            UserDAO userDAO = mock(UserDAO.class);
            when(userDAO.selectByUsername("missing")).thenReturn(null);

            try (MockedStatic<UserDAO> mockedUserDAO = mockStatic(UserDAO.class)) {
                mockedUserDAO.when(UserDAO::getInstance).thenReturn(userDAO);

                assertThrows(
                        InvalidCredentialsException.class,
                        () -> UserManager.getInstance().authenticate("missing", "pass123"));
                verify(userDAO).selectByUsername("missing");
            }
        }

        @Test
        @DisplayName("sai đăng nhập khi mật khẩu không khớp")
        void authenticate_throwsInvalidCredentialsWhenPasswordDiffers() throws Exception {
            User user = alice();
            UserDAO userDAO = mock(UserDAO.class);
            when(userDAO.selectByUsername("alice")).thenReturn(user);

            try (MockedStatic<UserDAO> mockedUserDAO = mockStatic(UserDAO.class)) {
                mockedUserDAO.when(UserDAO::getInstance).thenReturn(userDAO);

                assertThrows(
                        InvalidCredentialsException.class,
                        () -> UserManager.getInstance().authenticate("alice", "wrong"));
                verify(userDAO).selectByUsername("alice");
                verify(userDAO, never()).update(any(User.class));
            }
        }

        @Test
        @DisplayName("mật khẩu phân biệt chữ hoa chữ thường")
        void authenticate_isCaseSensitive() throws Exception {
            User user = alice();
            UserDAO userDAO = mock(UserDAO.class);
            when(userDAO.selectByUsername("alice")).thenReturn(user);

            try (MockedStatic<UserDAO> mockedUserDAO = mockStatic(UserDAO.class)) {
                mockedUserDAO.when(UserDAO::getInstance).thenReturn(userDAO);

                assertThrows(
                        InvalidCredentialsException.class,
                        () -> UserManager.getInstance().authenticate("alice", "Pass123"));
                verify(userDAO).selectByUsername("alice");
                verify(userDAO, never()).update(any(User.class));
            }
        }

        @Test
        @DisplayName("mật khẩu null bị từ chối")
        void authenticate_throwsInvalidCredentialsWhenInputPasswordIsNull() throws Exception {
            User user = alice();
            UserDAO userDAO = mock(UserDAO.class);
            when(userDAO.selectByUsername("alice")).thenReturn(user);

            try (MockedStatic<UserDAO> mockedUserDAO = mockStatic(UserDAO.class)) {
                mockedUserDAO.when(UserDAO::getInstance).thenReturn(userDAO);

                assertThrows(
                        InvalidCredentialsException.class,
                        () -> UserManager.getInstance().authenticate("alice", null));
                verify(userDAO).selectByUsername("alice");
                verify(userDAO, never()).update(any(User.class));
            }
        }

        @Test
        @DisplayName("mật khẩu đã hash xác thực được và không migrate lại")
        void authenticate_acceptsStoredHashWithoutMigratingAgain() throws Exception {
            User user =
                    new User(
                            1,
                            "alice",
                            PasswordHasher.hash("pass123"),
                            "alice@example.com",
                            "0901234567",
                            AccountType.USER,
                            null);
            UserDAO userDAO = mock(UserDAO.class);
            when(userDAO.selectByUsername("alice")).thenReturn(user);

            try (MockedStatic<UserDAO> mockedUserDAO = mockStatic(UserDAO.class)) {
                mockedUserDAO.when(UserDAO::getInstance).thenReturn(userDAO);

                User result = UserManager.getInstance().authenticate("alice", "pass123");

                assertSame(user, result);
                assertTrue(PasswordHasher.isHashed(result.getPassWord()));
                assertTrue(PasswordHasher.matches("pass123", result.getPassWord()));
                verify(userDAO, never()).update(any(User.class));
            }
        }
    }

    @Nested
    @DisplayName("UserManager.updateUserProfile")
    class UpdateUserProfileManagerTest {
        @Test
        @DisplayName("giữ dữ liệu cũ khi input null hoặc blank")
        void updateUserProfile_keepsOldValuesWhenInputIsNullOrBlank() throws Exception {
            User oldUser = alice();
            UserDAO userDAO = mock(UserDAO.class);
            when(userDAO.selectById(1)).thenReturn(oldUser);
            when(userDAO.update(any(User.class))).thenReturn(1);

            try (MockedStatic<UserDAO> mockedUserDAO = mockStatic(UserDAO.class)) {
                mockedUserDAO.when(UserDAO::getInstance).thenReturn(userDAO);

                User result =
                        UserManager.getInstance().updateUserProfile(1, null, "   ", null, null);

                assertNotNull(result);
                assertEquals("alice", result.getName());
                assertEquals("alice@example.com", result.getEmail());
                assertEquals("0901234567", result.getSdt());
                assertEquals("alice.png", result.getImagePath());
                verify(userDAO).update(any(User.class));
            }
        }

        @Test
        @DisplayName("trim tên, email và số điện thoại khi có input mới")
        void updateUserProfile_trimsNameEmailAndPhoneWhenProvided() throws Exception {
            User oldUser = alice();
            UserDAO userDAO = mock(UserDAO.class);
            when(userDAO.selectById(1)).thenReturn(oldUser);
            when(userDAO.update(any(User.class))).thenReturn(1);

            try (MockedStatic<UserDAO> mockedUserDAO = mockStatic(UserDAO.class)) {
                mockedUserDAO.when(UserDAO::getInstance).thenReturn(userDAO);

                User result =
                        UserManager.getInstance()
                                .updateUserProfile(
                                        1,
                                        "  Alice Updated  ",
                                        "  updated@example.com  ",
                                        "  0999888777  ",
                                        "updated.png");

                assertNotNull(result);
                assertEquals("Alice Updated", result.getName());
                assertEquals("updated@example.com", result.getEmail());
                assertEquals("0999888777", result.getSdt());
                assertEquals("updated.png", result.getImagePath());
            }
        }

        @Test
        @DisplayName("update profile không đổi id, mật khẩu và role")
        void updateUserProfile_preservesIdPasswordAndRole() throws Exception {
            User oldUser = alice();
            UserDAO userDAO = mock(UserDAO.class);
            when(userDAO.selectById(1)).thenReturn(oldUser);
            when(userDAO.update(any(User.class))).thenReturn(1);

            try (MockedStatic<UserDAO> mockedUserDAO = mockStatic(UserDAO.class)) {
                mockedUserDAO.when(UserDAO::getInstance).thenReturn(userDAO);

                User result =
                        UserManager.getInstance()
                                .updateUserProfile(1, "new", "new@example.com", "0999", "new.png");

                assertNotNull(result);
                assertEquals(oldUser.getId(), result.getId());
                assertEquals(oldUser.getPassWord(), result.getPassWord());
                assertEquals(oldUser.getAccountType(), result.getAccountType());
            }
        }

        @Test
        @DisplayName("gửi user đã merge xuống DAO")
        void updateUserProfile_sendsMergedUserToDao() throws Exception {
            User oldUser = alice();
            UserDAO userDAO = mock(UserDAO.class);
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            when(userDAO.selectById(1)).thenReturn(oldUser);
            when(userDAO.update(userCaptor.capture())).thenReturn(1);

            try (MockedStatic<UserDAO> mockedUserDAO = mockStatic(UserDAO.class)) {
                mockedUserDAO.when(UserDAO::getInstance).thenReturn(userDAO);

                User result =
                        UserManager.getInstance()
                                .updateUserProfile(1, "new", "new@example.com", "0999", "");

                assertNotNull(result);
                User sentToDao = userCaptor.getValue();
                assertEquals(1, sentToDao.getId());
                assertEquals("new", sentToDao.getName());
                assertEquals("pass123", sentToDao.getPassWord());
                assertEquals("new@example.com", sentToDao.getEmail());
                assertEquals("0999", sentToDao.getSdt());
                assertEquals(AccountType.USER, sentToDao.getAccountType());
                assertEquals("", sentToDao.getImagePath());
            }
        }

        @Test
        @DisplayName("user không tồn tại thì không update DAO")
        void updateUserProfile_returnsNullWhenUserDoesNotExist() throws Exception {
            UserDAO userDAO = mock(UserDAO.class);
            when(userDAO.selectById(404)).thenReturn(null);

            try (MockedStatic<UserDAO> mockedUserDAO = mockStatic(UserDAO.class)) {
                mockedUserDAO.when(UserDAO::getInstance).thenReturn(userDAO);

                assertNull(
                        UserManager.getInstance()
                                .updateUserProfile(
                                        404, "new", "new@example.com", "0999", "new.png"));
                verify(userDAO, never()).update(any(User.class));
            }
        }

        @Test
        @DisplayName("DAO update thất bại thì trả null")
        void updateUserProfile_returnsNullWhenDaoUpdateFails() throws Exception {
            User oldUser = alice();
            UserDAO userDAO = mock(UserDAO.class);
            when(userDAO.selectById(1)).thenReturn(oldUser);
            when(userDAO.update(any(User.class))).thenReturn(0);

            try (MockedStatic<UserDAO> mockedUserDAO = mockStatic(UserDAO.class)) {
                mockedUserDAO.when(UserDAO::getInstance).thenReturn(userDAO);

                assertNull(
                        UserManager.getInstance()
                                .updateUserProfile(1, "new", "new@example.com", "0999", "new.png"));
                verify(userDAO).update(any(User.class));
            }
        }
    }

    @Nested
    @DisplayName("UserManager.reset_password")
    class ResetPasswordManagerTest {
        @Test
        @DisplayName("reset password đổi mật khẩu và giữ profile")
        void resetPassword_updatesPasswordAndKeepsProfileFields() throws Exception {
            User oldUser = alice();
            UserDAO userDAO = mock(UserDAO.class);
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            when(userDAO.selectById(1)).thenReturn(oldUser);
            when(userDAO.update(userCaptor.capture())).thenReturn(1);

            try (MockedStatic<UserDAO> mockedUserDAO = mockStatic(UserDAO.class)) {
                mockedUserDAO.when(UserDAO::getInstance).thenReturn(userDAO);

                User reset = UserManager.getInstance().reset_password(1, "new-secret");

                assertNotNull(reset);
                assertEquals(1, reset.getId());
                assertEquals("alice", reset.getName());
                assertEquals("alice@example.com", reset.getEmail());
                assertEquals("0901234567", reset.getSdt());
                assertEquals(AccountType.USER, reset.getAccountType());
                assertEquals("alice.png", reset.getImagePath());
                assertEquals("new-secret", reset.getPassWord());

                User sentToDao = userCaptor.getValue();
                assertEquals(reset.getId(), sentToDao.getId());
                assertEquals(reset.getPassWord(), sentToDao.getPassWord());
            }
        }

        @Test
        @DisplayName("reset password trả null khi DAO update thất bại")
        void resetPassword_returnsNullWhenDaoUpdateFails() throws Exception {
            User oldUser = alice();
            UserDAO userDAO = mock(UserDAO.class);
            when(userDAO.selectById(1)).thenReturn(oldUser);
            when(userDAO.update(any(User.class))).thenReturn(0);

            try (MockedStatic<UserDAO> mockedUserDAO = mockStatic(UserDAO.class)) {
                mockedUserDAO.when(UserDAO::getInstance).thenReturn(userDAO);

                assertNull(UserManager.getInstance().reset_password(1, "new-secret"));
                verify(userDAO).update(any(User.class));
            }
        }
    }

    @Nested
    @DisplayName("UserManager.deleteUser")
    class DeleteUserManagerTest {
        @Test
        @DisplayName("xóa user thành công khi DAO delete thành công")
        void deleteUser_returnsTrueWhenDaoDeleteSucceeds() throws Exception {
            User user = alice();
            UserDAO userDAO = mock(UserDAO.class);
            when(userDAO.selectById(1)).thenReturn(user);
            when(userDAO.delete(user)).thenReturn(1);

            try (MockedStatic<UserDAO> mockedUserDAO = mockStatic(UserDAO.class)) {
                mockedUserDAO.when(UserDAO::getInstance).thenReturn(userDAO);

                assertTrue(
                        UserManager.getInstance().deleteUser(1, "ignored", "ignored", "ignored"));
                verify(userDAO).selectById(1);
                verify(userDAO).delete(user);
            }
        }

        @Test
        @DisplayName("xóa user thất bại khi DAO delete trả 0")
        void deleteUser_returnsFalseWhenDaoDeleteFails() throws Exception {
            User user = alice();
            UserDAO userDAO = mock(UserDAO.class);
            when(userDAO.selectById(1)).thenReturn(user);
            when(userDAO.delete(user)).thenReturn(0);

            try (MockedStatic<UserDAO> mockedUserDAO = mockStatic(UserDAO.class)) {
                mockedUserDAO.when(UserDAO::getInstance).thenReturn(userDAO);

                assertFalse(
                        UserManager.getInstance().deleteUser(1, "ignored", "ignored", "ignored"));
                verify(userDAO).delete(user);
            }
        }
    }
}
