package com.javfxtutorial.hethongdaugia.server.manager;

import com.javfxtutorial.hethongdaugia.common.model.User;
import com.javfxtutorial.hethongdaugia.common.model.enums.AccountType;
import com.javfxtutorial.hethongdaugia.server.dao.UserDAO;
import com.javfxtutorial.hethongdaugia.server.security.PasswordHasher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class UserContractTest {
    private static final User ALICE = new User(
            1,
            "alice",
            "pass123",
            "alice@example.com",
            "0901234567",
            AccountType.USER,
            "alice.png"
    );

    @Nested
    @DisplayName("User model")
    class UserModelTest {
        @Test
        void constructorWithoutId_setsCoreFieldsAndNullAvatar() {
            User user = new User("seller", "secret", "seller@example.com", "0911", AccountType.USER);

            assertEquals(0, user.getId());
            assertEquals("seller", user.getName());
            assertEquals("secret", user.getPassWord());
            assertEquals("seller@example.com", user.getEmail());
            assertEquals("0911", user.getSdt());
            assertEquals(AccountType.USER, user.getAccountType());
            assertNull(user.getImagePath());
        }

        @Test
        void constructorWithIdAndNoAvatar_setsAvatarNull() {
            User user = new User(2, "admin", "root", "admin@example.com", "0999", AccountType.ADMIN);

            assertEquals(2, user.getId());
            assertEquals("admin", user.getName());
            assertEquals("root", user.getPassWord());
            assertEquals("admin@example.com", user.getEmail());
            assertEquals("0999", user.getSdt());
            assertEquals(AccountType.ADMIN, user.getAccountType());
            assertNull(user.getImagePath());
        }

        @Test
        void constructorWithAvatar_setsAllFields() {
            User user = new User(3, "bob", "pw", "bob@example.com", "0888", AccountType.USER, "bob.png");

            assertEquals(3, user.getId());
            assertEquals("bob", user.getName());
            assertEquals("pw", user.getPassWord());
            assertEquals("bob@example.com", user.getEmail());
            assertEquals("0888", user.getSdt());
            assertEquals(AccountType.USER, user.getAccountType());
            assertEquals("bob.png", user.getImagePath());
        }

        @Test
        void setters_updateMutableFields() {
            User user = new User("old", "oldpw", "old@example.com", "0900", AccountType.USER);

            user.setId(99);
            user.setName("new");
            user.setPassWord("newpw");
            user.setEmail("new@example.com");
            user.setSdt("0911");
            user.setImagePath("new.png");

            assertEquals(99, user.getId());
            assertEquals("new", user.getName());
            assertEquals("newpw", user.getPassWord());
            assertEquals("new@example.com", user.getEmail());
            assertEquals("0911", user.getSdt());
            assertEquals("new.png", user.getImagePath());
        }

        @Test
        void toString_containsVisibleUserFields() {
            String text = ALICE.toString();

            assertTrue(text.contains("id=1"));
            assertTrue(text.contains("alice"));
            assertTrue(text.contains("alice@example.com"));
            assertTrue(text.contains("0901234567"));
            assertTrue(text.contains("USER"));
        }
    }

    @Nested
    @DisplayName("AccountType")
    class AccountTypeTest {
        @Test
        void accountType_containsAdminAndUserRoles() {
            Set<String> names = Arrays.stream(AccountType.values())
                    .map(Enum::name)
                    .collect(Collectors.toSet());

            assertEquals(Set.of("ADMIN", "USER"), names);
        }
    }

    @Nested
    @DisplayName("UserManager singleton")
    class UserManagerSingletonTest {
        @Test
        void getInstance_returnsSameObject() {
            assertSame(UserManager.getInstance(), UserManager.getInstance());
        }

        @Test
        void constructor_isPrivate() throws NoSuchMethodException {
            Constructor<UserManager> constructor = UserManager.class.getDeclaredConstructor();
            assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        }
    }

    @Nested
    @DisplayName("UserManager.authenticate")
    class AuthenticationManagerTest {
        @Test
        void authenticate_returnsUserWhenPasswordMatches() {
            UserDAO userDAO = mock(UserDAO.class);
            when(userDAO.selectByUsername("alice")).thenReturn(ALICE);

            try (MockedStatic<UserDAO> mockedUserDAO = mockStatic(UserDAO.class)) {
                mockedUserDAO.when(UserDAO::getInstance).thenReturn(userDAO);

                User result = UserManager.getInstance().authenticate("alice", "pass123");

                assertSame(ALICE, result);
                verify(userDAO).selectByUsername("alice");
            }
        }

        @Test
        void authenticate_returnsNullWhenUserMissing() {
            UserDAO userDAO = mock(UserDAO.class);
            when(userDAO.selectByUsername("missing")).thenReturn(null);

            try (MockedStatic<UserDAO> mockedUserDAO = mockStatic(UserDAO.class)) {
                mockedUserDAO.when(UserDAO::getInstance).thenReturn(userDAO);

                assertNull(UserManager.getInstance().authenticate("missing", "pass123"));
                verify(userDAO).selectByUsername("missing");
            }
        }

        @Test
        void authenticate_returnsNullWhenPasswordDiffers() {
            UserDAO userDAO = mock(UserDAO.class);
            when(userDAO.selectByUsername("alice")).thenReturn(ALICE);

            try (MockedStatic<UserDAO> mockedUserDAO = mockStatic(UserDAO.class)) {
                mockedUserDAO.when(UserDAO::getInstance).thenReturn(userDAO);

                assertNull(UserManager.getInstance().authenticate("alice", "wrong"));
                verify(userDAO).selectByUsername("alice");
            }
        }

        @Test
        void authenticate_isCaseSensitive() {
            UserDAO userDAO = mock(UserDAO.class);
            when(userDAO.selectByUsername("alice")).thenReturn(ALICE);

            try (MockedStatic<UserDAO> mockedUserDAO = mockStatic(UserDAO.class)) {
                mockedUserDAO.when(UserDAO::getInstance).thenReturn(userDAO);

                assertNull(UserManager.getInstance().authenticate("alice", "Pass123"));
                verify(userDAO).selectByUsername("alice");
            }
        }

        @Test
        void authenticate_returnsNullWhenInputPasswordIsNull() {
            UserDAO userDAO = mock(UserDAO.class);
            when(userDAO.selectByUsername("alice")).thenReturn(ALICE);

            try (MockedStatic<UserDAO> mockedUserDAO = mockStatic(UserDAO.class)) {
                mockedUserDAO.when(UserDAO::getInstance).thenReturn(userDAO);

                assertNull(UserManager.getInstance().authenticate("alice", null));
                verify(userDAO).selectByUsername("alice");
            }
        }

        @Test
        void authenticate_acceptsStoredHash() {
            User user = new User(1, "alice", PasswordHasher.hash("pass123"), "alice@example.com", "0901234567", AccountType.USER, null);
            UserDAO userDAO = mock(UserDAO.class);
            when(userDAO.selectByUsername("alice")).thenReturn(user);

            try (MockedStatic<UserDAO> mockedUserDAO = mockStatic(UserDAO.class)) {
                mockedUserDAO.when(UserDAO::getInstance).thenReturn(userDAO);

                assertSame(user, UserManager.getInstance().authenticate("alice", "pass123"));
                verify(userDAO, never()).update(any(User.class));
            }
        }

        @Test
        void authenticate_migratesLegacyPlainTextPasswordAfterSuccessfulLogin() {
            User legacyUser = new User(1, "alice", "pass123", "alice@example.com", "0901234567", AccountType.USER, null);
            UserDAO userDAO = mock(UserDAO.class);
            when(userDAO.selectByUsername("alice")).thenReturn(legacyUser);
            when(userDAO.update(any(User.class))).thenReturn(1);

            try (MockedStatic<UserDAO> mockedUserDAO = mockStatic(UserDAO.class)) {
                mockedUserDAO.when(UserDAO::getInstance).thenReturn(userDAO);

                assertSame(legacyUser, UserManager.getInstance().authenticate("alice", "pass123"));
                verify(userDAO).update(legacyUser);
            }
        }
    }

    @Nested
    @DisplayName("UserManager.updateUserProfile")
    class UpdateUserProfileManagerTest {
        @Test
        void updateUserProfile_keepsOldValuesWhenInputIsNullOrBlank() {
            UserDAO userDAO = mock(UserDAO.class);
            when(userDAO.selectById(1)).thenReturn(ALICE);
            when(userDAO.update(any(User.class))).thenReturn(1);

            try (MockedStatic<UserDAO> mockedUserDAO = mockStatic(UserDAO.class)) {
                mockedUserDAO.when(UserDAO::getInstance).thenReturn(userDAO);

                User result = UserManager.getInstance().updateUserProfile(1, null, "   ", null, null);

                assertNotNull(result);
                assertEquals("alice", result.getName());
                assertEquals("alice@example.com", result.getEmail());
                assertEquals("0901234567", result.getSdt());
                assertEquals("alice.png", result.getImagePath());
                verify(userDAO).update(any(User.class));
            }
        }

        @Test
        void updateUserProfile_trimsNameEmailAndPhoneWhenProvided() {
            UserDAO userDAO = mock(UserDAO.class);
            when(userDAO.selectById(1)).thenReturn(ALICE);
            when(userDAO.update(any(User.class))).thenReturn(1);

            try (MockedStatic<UserDAO> mockedUserDAO = mockStatic(UserDAO.class)) {
                mockedUserDAO.when(UserDAO::getInstance).thenReturn(userDAO);

                User result = UserManager.getInstance().updateUserProfile(
                        1,
                        "  Alice Updated  ",
                        "  updated@example.com  ",
                        "  0999888777  ",
                        "updated.png"
                );

                assertNotNull(result);
                assertEquals("Alice Updated", result.getName());
                assertEquals("updated@example.com", result.getEmail());
                assertEquals("0999888777", result.getSdt());
                assertEquals("updated.png", result.getImagePath());
            }
        }

        @Test
        void updateUserProfile_preservesIdPasswordAndRole() {
            UserDAO userDAO = mock(UserDAO.class);
            when(userDAO.selectById(1)).thenReturn(ALICE);
            when(userDAO.update(any(User.class))).thenReturn(1);

            try (MockedStatic<UserDAO> mockedUserDAO = mockStatic(UserDAO.class)) {
                mockedUserDAO.when(UserDAO::getInstance).thenReturn(userDAO);

                User result = UserManager.getInstance().updateUserProfile(1, "new", "new@example.com", "0999", "new.png");

                assertNotNull(result);
                assertEquals(ALICE.getId(), result.getId());
                assertEquals(ALICE.getPassWord(), result.getPassWord());
                assertEquals(ALICE.getAccountType(), result.getAccountType());
            }
        }

        @Test
        void updateUserProfile_sendsMergedUserToDao() {
            UserDAO userDAO = mock(UserDAO.class);
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            when(userDAO.selectById(1)).thenReturn(ALICE);
            when(userDAO.update(userCaptor.capture())).thenReturn(1);

            try (MockedStatic<UserDAO> mockedUserDAO = mockStatic(UserDAO.class)) {
                mockedUserDAO.when(UserDAO::getInstance).thenReturn(userDAO);

                User result = UserManager.getInstance().updateUserProfile(1, "new", "new@example.com", "0999", "");

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
        void updateUserProfile_returnsNullWhenUserDoesNotExist() {
            UserDAO userDAO = mock(UserDAO.class);
            when(userDAO.selectById(404)).thenReturn(null);

            try (MockedStatic<UserDAO> mockedUserDAO = mockStatic(UserDAO.class)) {
                mockedUserDAO.when(UserDAO::getInstance).thenReturn(userDAO);

                assertNull(UserManager.getInstance().updateUserProfile(404, "new", "new@example.com", "0999", "new.png"));
                verify(userDAO, never()).update(any(User.class));
            }
        }

        @Test
        void updateUserProfile_returnsNullWhenDaoUpdateFails() {
            UserDAO userDAO = mock(UserDAO.class);
            when(userDAO.selectById(1)).thenReturn(ALICE);
            when(userDAO.update(any(User.class))).thenReturn(0);

            try (MockedStatic<UserDAO> mockedUserDAO = mockStatic(UserDAO.class)) {
                mockedUserDAO.when(UserDAO::getInstance).thenReturn(userDAO);

                assertNull(UserManager.getInstance().updateUserProfile(1, "new", "new@example.com", "0999", "new.png"));
                verify(userDAO).update(any(User.class));
            }
        }
    }

    @Nested
    @DisplayName("UserManager.reset_password")
    class ResetPasswordManagerTest {
        @Test
        void resetPassword_changesOnlyPassword() {
            UserDAO userDAO = mock(UserDAO.class);
            when(userDAO.selectById(1)).thenReturn(ALICE);
            when(userDAO.update(any(User.class))).thenReturn(1);

            try (MockedStatic<UserDAO> mockedUserDAO = mockStatic(UserDAO.class)) {
                mockedUserDAO.when(UserDAO::getInstance).thenReturn(userDAO);

                User reset = UserManager.getInstance().reset_password(1, "new-secret");

                assertNotNull(reset);
                assertEquals(ALICE.getId(), reset.getId());
                assertEquals(ALICE.getName(), reset.getName());
                assertEquals("new-secret", reset.getPassWord());
                assertEquals(ALICE.getEmail(), reset.getEmail());
                assertEquals(ALICE.getSdt(), reset.getSdt());
                assertEquals(ALICE.getAccountType(), reset.getAccountType());
                assertEquals(ALICE.getImagePath(), reset.getImagePath());
                verify(userDAO).update(any(User.class));
            }
        }

        @Test
        void resetPassword_allowsEmptyPasswordByCurrentContract() {
            UserDAO userDAO = mock(UserDAO.class);
            when(userDAO.selectById(1)).thenReturn(ALICE);
            when(userDAO.update(any(User.class))).thenReturn(1);

            try (MockedStatic<UserDAO> mockedUserDAO = mockStatic(UserDAO.class)) {
                mockedUserDAO.when(UserDAO::getInstance).thenReturn(userDAO);

                User reset = UserManager.getInstance().reset_password(1, "");

                assertNotNull(reset);
                assertEquals("", reset.getPassWord());
            }
        }

        @Test
        void resetPassword_returnsNullWhenDaoUpdateFails() {
            UserDAO userDAO = mock(UserDAO.class);
            when(userDAO.selectById(1)).thenReturn(ALICE);
            when(userDAO.update(any(User.class))).thenReturn(0);

            try (MockedStatic<UserDAO> mockedUserDAO = mockStatic(UserDAO.class)) {
                mockedUserDAO.when(UserDAO::getInstance).thenReturn(userDAO);

                assertNull(UserManager.getInstance().reset_password(1, "new-secret"));
                verify(userDAO).update(any(User.class));
            }
        }
    }

    @Nested
    @DisplayName("UserManager.checkExistedUsername")
    class CheckExistedUsernameManagerTest {
        @Test
        void existedUsername_returnsTrueWithoutDaoLookup() {
            UserDAO userDAO = mock(UserDAO.class);

            try (MockedStatic<UserDAO> mockedUserDAO = mockStatic(UserDAO.class)) {
                mockedUserDAO.when(UserDAO::getInstance).thenReturn(userDAO);

                assertTrue(UserManager.getInstance().checkExistedUsername("alice"));
                verifyNoInteractions(userDAO);
            }
        }

        @Test
        void existedUsername_returnsTrueEvenWhenDaoWouldReturnNull() {
            UserDAO userDAO = mock(UserDAO.class);

            try (MockedStatic<UserDAO> mockedUserDAO = mockStatic(UserDAO.class)) {
                mockedUserDAO.when(UserDAO::getInstance).thenReturn(userDAO);

                assertTrue(UserManager.getInstance().checkExistedUsername("missing"));
                verifyNoInteractions(userDAO);
            }
        }
    }

    @Nested
    @DisplayName("UserManager.deleteUser")
    class DeleteUserManagerTest {
        @Test
        void deleteUser_returnsTrueWhenDaoDeleteSucceeds() {
            UserDAO userDAO = mock(UserDAO.class);
            when(userDAO.selectById(1)).thenReturn(ALICE);
            when(userDAO.delete(ALICE)).thenReturn(1);

            try (MockedStatic<UserDAO> mockedUserDAO = mockStatic(UserDAO.class)) {
                mockedUserDAO.when(UserDAO::getInstance).thenReturn(userDAO);

                assertTrue(UserManager.getInstance().deleteUser(1, "ignored", "ignored", "ignored"));
                verify(userDAO).selectById(1);
                verify(userDAO).delete(ALICE);
            }
        }

        @Test
        void deleteUser_returnsFalseWhenDaoDeleteFails() {
            UserDAO userDAO = mock(UserDAO.class);
            when(userDAO.selectById(1)).thenReturn(ALICE);
            when(userDAO.delete(ALICE)).thenReturn(0);

            try (MockedStatic<UserDAO> mockedUserDAO = mockStatic(UserDAO.class)) {
                mockedUserDAO.when(UserDAO::getInstance).thenReturn(userDAO);

                assertFalse(UserManager.getInstance().deleteUser(1, "ignored", "ignored", "ignored"));
                verify(userDAO).delete(ALICE);
            }
        }
    }
}
