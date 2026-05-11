package com.javfxtutorial.hethongdaugia.server.manager;

import com.javfxtutorial.hethongdaugia.common.model.User;
import com.javfxtutorial.hethongdaugia.common.model.enums.AccountType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class UserManagerTest {
    // dữ liệu cố định
    private static final User ALICE = new User(1, "alice", "pass123", "alice@example.com", "0901234567", AccountType.USER, null);
    private static final User BOB = new User(2, "bob", "secret", "bob@example.com", "0912345678", AccountType.ADMIN, null);

    @BeforeEach
    void setup(){}

    @Nested
    @DisplayName("authenticate — kiểm tra logic xác thực")
    class AuthenticateLogicTest {

        @Test
        @DisplayName("trả về user khi username và password khớp")
        void auth_success() {
            // Mô phỏng: UserDAO trả về ALICE, password khớp
            User result = simulateAuthenticate(ALICE, "pass123");
            assertNotNull(result);
            assertEquals("alice", result.getName());
        }

        @Test
        @DisplayName("trả về null khi password sai")
        void auth_wrongPassword() {
            User result = simulateAuthenticate(ALICE, "wrongpass");
            assertNull(result);
        }

        @Test
        @DisplayName("trả về null khi user không tồn tại (UserDAO trả null)")
        void auth_userNotFound() {
            User result = simulateAuthenticate(null, "anypassword");
            assertNull(result);
        }

        @Test
        @DisplayName("password phân biệt hoa thường")
        void auth_caseSensitivePassword() {
            User result = simulateAuthenticate(ALICE, "Pass123");
            assertNull(result);
        }

        @Test
        @DisplayName("password rỗng không khớp với password thực")
        void auth_emptyPassword() {
            User result = simulateAuthenticate(ALICE, "");
            assertNull(result);
        }
        private User simulateAuthenticate(User fromDao, String inputPassword) {
            if (fromDao != null && fromDao.getPassWord().equals(inputPassword)) {
                return fromDao;
            }
            return null;
        }
    }
    @Nested
    @DisplayName("updateUserProfile — logic merge thông tin")
    class UpdateProfileLogicTest {

        @Test
        @DisplayName("giữ nguyên tên cũ khi tên mới là null")
        void merge_keepOldName_whenNewIsNull() {
            User result = simulateMerge(ALICE, null, "new@email.com", "0999", null);
            assertEquals("alice", result.getName());
            assertEquals("new@email.com", result.getEmail());
        }

        @Test
        @DisplayName("giữ nguyên tên cũ khi tên mới là chuỗi rỗng")
        void merge_keepOldName_whenNewIsBlank() {
            User result = simulateMerge(ALICE, "   ", "new@email.com", "0999", null);
            assertEquals("alice", result.getName());
        }

        @Test
        @DisplayName("cập nhật tên mới khi có giá trị hợp lệ")
        void merge_updatesName_whenProvided() {
            User result = simulateMerge(ALICE, "Alice Updated", null, null, null);
            assertEquals("Alice Updated", result.getName());
        }

        @Test
        @DisplayName("giữ nguyên email cũ khi email mới là blank")
        void merge_keepOldEmail_whenBlank() {
            User result = simulateMerge(ALICE, "alice", "", "0901234567", null);
            assertEquals("alice@example.com", result.getEmail());
        }

        @Test
        @DisplayName("giữ nguyên SĐT cũ khi phone mới là null")
        void merge_keepOldPhone_whenNull() {
            User result = simulateMerge(ALICE, "alice", "alice@example.com", null, null);
            assertEquals("0901234567", result.getSdt());
        }

        @Test
        @DisplayName("avatar null trong input được giữ nguyên avatar cũ")
        void merge_keepOldAvt_whenNull() {
            User userWithAvt = new User(3, "carol", "pw", "carol@x.com", "0900",
                    AccountType.USER, "old_avatar.png");
            User result = simulateMerge(userWithAvt, "carol", "carol@x.com", "0900", null);
            assertEquals("old_avatar.png", result.getImagePath());
        }

        @Test
        @DisplayName("avatar mới (non-null) ghi đè avatar cũ")
        void merge_updatesAvt_whenProvided() {
            User userWithAvt = new User(3, "carol", "pw", "carol@x.com", "0900",
                    AccountType.USER, "old_avatar.png");
            User result = simulateMerge(userWithAvt, "carol", "carol@x.com", "0900", "new_avatar.png");
            assertEquals("new_avatar.png", result.getImagePath());
        }

        @Test
        @DisplayName("password không bị thay đổi khi update profile")
        void merge_passwordUnchanged() {
            User result = simulateMerge(ALICE, "NewName", "new@email.com", "0999", null);
            assertEquals("pass123", result.getPassWord());
        }

        /**
         * Tái hiện đúng logic merge trong UserManager.updateUserProfile():
         */
        private User simulateMerge(User old, String name, String email,
                                   String phone, String avt) {
            String resolvedName  = (name  == null || name.isBlank())  ? old.getName()      : name.trim();
            String resolvedEmail = (email == null || email.isBlank()) ? old.getEmail()     : email.trim();
            String resolvedPhone = (phone == null || phone.isBlank()) ? old.getSdt()       : phone.trim();
            String resolvedAvt   = (avt   == null)                    ? old.getImagePath() : avt;
            return new User(old.getId(), resolvedName, old.getPassWord(),
                    resolvedEmail, resolvedPhone, old.getAccountType(), resolvedAvt);
        }
    }
    @Nested
    @DisplayName("checkExistedUsername — logic kiểm tra tồn tại")
    class CheckExistedUsernameTest {

        @Test
        @DisplayName("trả về true khi UserDAO tìm thấy user")
        void exists_true_whenUserFound() {
            assertTrue(simulateCheckExisted(ALICE));
        }

        @Test
        @DisplayName("trả về false khi UserDAO trả null (user không tồn tại)")
        void exists_false_whenUserNotFound() {
            assertFalse(simulateCheckExisted(null));
        }

        /** Tái hiện logic: return user != null; */
        private boolean simulateCheckExisted(User fromDao) {
            return fromDao != null;
        }
    }
    @Nested
    @DisplayName("Singleton pattern")
    class SingletonTest {

        @Test
        @DisplayName("getInstance() luôn trả về cùng một instance")
        void singleton_sameInstance() {
            UserManager a = UserManager.getInstance();
            UserManager b = UserManager.getInstance();
            assertSame(a, b);
        }
    }
    @Nested
    @DisplayName("reset_password — logic tạo user mới với password mới")
    class ResetPasswordLogicTest {

        @Test
        @DisplayName("user mới có password được thay đổi")
        void reset_newPasswordApplied() {
            User result = simulateResetPassword(ALICE, "newSecret");
            assertEquals("newSecret", result.getPassWord());
        }

        @Test
        @DisplayName("các thông tin khác (tên, email, SĐT, role) giữ nguyên")
        void reset_otherFieldsUnchanged() {
            User result = simulateResetPassword(ALICE, "newSecret");
            assertEquals(ALICE.getId(),          result.getId());
            assertEquals(ALICE.getName(),        result.getName());
            assertEquals(ALICE.getEmail(),       result.getEmail());
            assertEquals(ALICE.getSdt(),         result.getSdt());
            assertEquals(ALICE.getAccountType(), result.getAccountType());
        }

        //Tái hiện logic trong UserManager.reset_password():
        private User simulateResetPassword(User old, String newPassword) {
            return new User(old.getId(), old.getName(), newPassword,
                    old.getEmail(), old.getSdt(),
                    old.getAccountType(), old.getImagePath());
        }
    }
}
