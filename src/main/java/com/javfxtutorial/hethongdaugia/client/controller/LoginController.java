package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.client.Util.UIUtils;
import com.javfxtutorial.hethongdaugia.client.Util.ThemeManager;
import com.javfxtutorial.hethongdaugia.client.model.ClientModel;
import com.javfxtutorial.hethongdaugia.client.network.NetworkManager;
import com.javfxtutorial.hethongdaugia.client.network.ResponseListener;
import com.javfxtutorial.hethongdaugia.client.network.ServerConnection;
import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.Command.GetUnpaidAuctionCommand;
import com.javfxtutorial.hethongdaugia.common.model.Command.LoginCommand;
import com.javfxtutorial.hethongdaugia.common.model.Command.UpdateAuctionStatusCommand;
import com.javfxtutorial.hethongdaugia.common.model.User;
import com.javfxtutorial.hethongdaugia.common.model.enums.AccountType;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import static com.javfxtutorial.hethongdaugia.client.Util.UIUtils.changeScene;

public class LoginController implements ResponseListener, Initializable {
    private static final Logger log = LoggerFactory.getLogger(LoginController.class);
    @FXML private TextField Username ;
    @FXML private PasswordField Password ;
    @FXML private Label message;
    ActionEvent loginEvent;
    @FXML private TextField PasswordVisible;
    private boolean passwordShown = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        NetworkManager.getInstance().start();
        Username.setOnAction(event -> Password.requestFocus());
        Password.setOnAction(this::clickLogin);
    }

    @FXML
    public void clickLogin(ActionEvent event) {
        loginEvent = event;
        String username = Username.getText();
        String password = Password.getText();
        new Thread(() -> {
            try {
                ServerConnection connection = NetworkManager.getConnection();
                Command cmd = new LoginCommand();
                cmd.addData("username", username);
                cmd.addData("password", password);
                NetworkManager.getInstance().register(cmd.getClass(), this);
                connection.sendCommand(cmd);
            } catch (Exception e) {
                log.error("Lỗi khi gửi login request: {}", e.getMessage(), e);
                Platform.runLater(() -> message.setText("Lỗi kết nối: " + e.getMessage()));
            }
        }).start();
    }


    public void clickCreateAccount(ActionEvent event) {
        changeScene(event,"/com/javfxtutorial/hethongdaugia/view/fxml/SignUp.fxml");
    }


    @Override
    public void onResponse(Response rp) {
        NetworkManager networkManager = NetworkManager.getInstance();
        networkManager.unregister(rp.getCommand().getClass(), this);
        if (rp.getCommand() instanceof LoginCommand) {
            if (rp.isSuccess()){
                User user = (User) rp.getPayLoad();
                ClientModel.getInstance().setCurrentUser(user);
                Platform.runLater(() -> {
                    if (user.getAccountType() == AccountType.USER) {
                        log.info(rp.getMessage());
                        Platform.runLater(this::checkUpaidAuction);
                    } else if (user.getAccountType() == AccountType.ADMIN) {
                        log.info(rp.getMessage());
                        changeScene(loginEvent, "/com/javfxtutorial/hethongdaugia/view/fxml/Admin_UserManagement.fxml");
                    }
                });}
            else {
                Platform.runLater(() -> message.setText(rp.getMessage()));
            }
        } else if (rp.getCommand() instanceof GetUnpaidAuctionCommand) {
            NetworkManager.getInstance().unregister(GetUnpaidAuctionCommand.class, this);
            ArrayList<Auction> unpaidList = (ArrayList<Auction>) rp.getPayLoad();

            Platform.runLater(() -> {
                if (unpaidList != null && !unpaidList.isEmpty()) {
                    // Hiện popup thanh toán tuần tự từng cái, block app
                    showPaymentPopupChain(unpaidList, 0);
                } else {
                    changeScene(loginEvent, "/com/javfxtutorial/hethongdaugia/view/fxml/MainScene.fxml");
                }
            });

        } else {
            Platform.runLater(() -> {
                message.setText("Sai tên hoặc mật khẩu!");
                log.info(rp.getMessage());
                changeScene(loginEvent, "/com/javfxtutorial/hethongdaugia/view/fxml/MainScene.fxml");
            });
        }
    }

    private void showPaymentPopupChain(ArrayList<Auction> unpaidList, int index) {
        if (index >= unpaidList.size()) {
            changeScene(loginEvent, "/com/javfxtutorial/hethongdaugia/view/fxml/MainScene.fxml");
            return;
        }

        Auction auction = unpaidList.get(index);
        try {
            FXMLLoader loader = new FXMLLoader(
                    UIUtils.class.getResource("/com/javfxtutorial/hethongdaugia/view/fxml/PaymentPopup.fxml"));
            Parent root = loader.load();
            PaymentPopupController ctrl = loader.getController();
            ctrl.setAuction(auction);

            Stage popup = new Stage();
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.initStyle(StageStyle.UNDECORATED);
            Scene scene = new Scene(root);
            ThemeManager.apply(scene);
            popup.setScene(scene);
            popup.setOnCloseRequest(Event::consume); // Chặn Alt+F4

            ctrl.setOnConfirmed(() -> {
                markAsPaid(auction);
                popup.close();
                showPaymentPopupChain(unpaidList, index + 1);
            });

            popup.show();
        } catch (IOException e) {
            log.error("Lỗi load PaymentPopup: {}", e.getMessage(), e);
            showPaymentPopupChain(unpaidList, index + 1);
        }
    }

    //    set laij trangj thais khi thanh toan xong
    private void markAsPaid(Auction auction) {
        new Thread(() -> {
            try {
                auction.setStatus(AuctionStatus.PAID);
                UpdateAuctionStatusCommand cmd = new UpdateAuctionStatusCommand(auction);
                NetworkManager.getConnection().sendCommand(cmd);

            } catch (Exception e) {
                log.error("Lỗi mark PAID: {}", e.getMessage());
            }
        }).start();
    }

    // check xem co auction chx thanh toan ko
    public void checkUpaidAuction(){
        int usedId = ClientModel.getInstance().getCurrentUser().getId();
        new Thread(() -> {
            try {
                ServerConnection serverConnection = NetworkManager.getConnection();
                NetworkManager.getInstance().register(GetUnpaidAuctionCommand.class, this);
                GetUnpaidAuctionCommand cmd =  new GetUnpaidAuctionCommand(usedId);
                serverConnection.sendCommand(cmd);
            } catch (Exception e) {
                log.error("Lỗi check unpaid: {}", e.getMessage());
                Platform.runLater(() ->
                        changeScene(loginEvent, "/com/javfxtutorial/hethongdaugia/view/fxml/MainScene.fxml"));
            }
        }).start();

    }



    // ẩn hiện mkh
    @FXML
    public void togglePasswordVisibility(ActionEvent event) {
        passwordShown = !passwordShown;
        if (passwordShown) {
            PasswordVisible.setText(Password.getText());
            PasswordVisible.setVisible(true);
            Password.setVisible(false);
        } else {
            Password.setText(PasswordVisible.getText());
            Password.setVisible(true);
            PasswordVisible.setVisible(false);
        }
    }


}
