package com.javfxtutorial.hethongdaugia.common.model.Command;

import com.javfxtutorial.hethongdaugia.common.Exception.data.QueryExecutionException;
import com.javfxtutorial.hethongdaugia.common.model.domain.User;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.UserDAO;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetAllUsersCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(GetAllUsersCommand.class);

    @Override
    public Response handle() {
        try {
            List<User> users = UserDAO.getInstance().selectAll();
            return new Response(true, "Lấy danh sách user thành công", users, this);
        } catch (QueryExecutionException e) {
            log.error("Lỗi truy vấn database: {}", e.getMessage(), e);
            return new Response(false, "Lỗi truy vấn dữ liệu", null, this);
        } catch (Exception e) {
            log.error("Lỗi không xác định: {}", e.getMessage(), e);
            return new Response(false, "Lỗi hệ thống: " + e.getMessage(), null, this);
        }
    }
}
