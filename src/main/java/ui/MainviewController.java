package ui;

import bank.PrivateBank;
import bank.exceptions.AccountAlreadyExistsException;
import bank.exceptions.AccountDoesNotExistException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainviewController {

    @FXML
    private ListView<String> lvAccounts;
    // Ini ListView di FXML (fx:id="lvAccounts").
    // Isi list-nya = nama-nama account (String).

    private PrivateBank bank;
    // Ini "mesin bank" yang pegang data + file persistence.
    // Controller cuma manggil method bank + update UI.

    // =========================================================
    // setBank = "INIT" controller (momen controller mulai hidup)
    // Dipanggil dari FxApplication setelah FXML di-load.
    // Tujuan:
    // 1) simpan bank yang sama (biar data konsisten)
    // 2) isi list accounts pertama kali (refreshAccounts)
    // 3) pasang context menu klik kanan (setupContextMenu)
    // =========================================================
    public void setBank(PrivateBank bank) {
        this.bank = bank;
        refreshAccounts();     // ambil semua akun dari bank -> tampilkan di list
        setupContextMenu();    // bikin klik kanan: Auswählen & Löschen
    }

    // =========================================================
    // refreshAccounts = sinkronin ListView dengan data di bank
    // Dipanggil:
    // - saat awal view dibuka (di setBank)
    // - setelah create account
    // - setelah delete account
    // =========================================================
    private void refreshAccounts() {
        if (bank == null) return;

        // bank.getAllAccounts() -> List<String> nama account
        // observableArrayList -> biar bisa dipakai ListView JavaFX
        lvAccounts.setItems(FXCollections.observableArrayList(bank.getAllAccounts()));
    }

    // =========================================================
    // setupContextMenu = bikin menu klik kanan pada ListView
    // Menu:
    // - Auswählen -> pindah ke Accountview untuk account terpilih
    // - Löschen   -> hapus account terpilih (dengan confirm)
    // =========================================================
    private void setupContextMenu() {
        MenuItem miSelect = new MenuItem("Auswählen");
        miSelect.setOnAction(e -> onSelectAccount());  // kalau diklik -> buka account detail

        MenuItem miDelete = new MenuItem("Löschen");
        miDelete.setOnAction(e -> onDeleteAccount());  // kalau diklik -> delete account

        ContextMenu cm = new ContextMenu(miSelect, miDelete);
        lvAccounts.setContextMenu(cm);
        // Artinya: kalau user klik kanan di list, menu ini muncul.
    }

    // =========================================================
    // getSelectedAccount = ambil account yang lagi dipilih user
    // Kenapa dibuat method sendiri?
    // biar onSelectAccount dan onDeleteAccount gak copy-paste logic yang sama.
    // =========================================================
    private String getSelectedAccount() {
        // selectionModel = sistem JavaFX buat nge-track item yang dipilih.
        String acc = lvAccounts.getSelectionModel().getSelectedItem();

        // kalau user belum pilih apa-apa -> error
        if (acc == null) {
            showError("Kein Account ausgewählt.");
        }
        return acc;
    }

    // =========================================================
    // onSelectAccount = handler untuk menu klik kanan "Auswählen"
    // Tujuan:
    // - pindah ke Accountview di STAGE yang sama (no new Stage!)
    // - kirim data: bank yang sama + nama account terpilih
    //
    // Flow:
    // 1) ambil selected account
    // 2) load accountview.fxml (bikin UI + controller)
    // 3) inject bank & account ke AccountviewController
    // 4) ganti scene di window yang sama
    // =========================================================
    private void onSelectAccount() {
        String acc = getSelectedAccount();
        if (acc == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/accountview.fxml"));
            Parent root = loader.load(); // load FXML -> bikin UI + bikin controller

            AccountviewController controller = loader.getController();
            controller.setBankAndAccount(bank, acc);
            // ini "passing data" antar scene:
            // accountview tau account mana yang harus ditampilkan.

            // ambil window yang lagi dipakai dari komponen UI:
            Stage stage = (Stage) lvAccounts.getScene().getWindow();

            // ganti isi window dengan scene accountview
            stage.setScene(new Scene(root));
            stage.setTitle("Bank - Account: " + acc);

        } catch (IOException e) {
            showError("Fehler beim Laden der Accountview: " + e.getMessage());
        }
    }

    // =========================================================
    // onDeleteAccount = handler untuk menu klik kanan "Löschen"
    // Tujuan:
    // - tanya confirm dulu (yes/no)
    // - kalau yes -> hapus di bank (termasuk file)
    // - refresh list biar UI update
    // =========================================================
    private void onDeleteAccount() {
        String acc = getSelectedAccount();
        if (acc == null) return;

        // dialog konfirmasi
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText(null);
        confirm.setContentText("Account \"" + acc + "\" wirklich löschen?");

        // kalau user bukan OK -> stop
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try {
            // hapus "beneran" dari sistem bank + persistence
            bank.deleteAccount(acc);

            // update list UI
            refreshAccounts();

        } catch (AccountDoesNotExistException | IOException e) {
            showError(e.getMessage());
        }
    }


    // =========================================================
    // onNewAccount = handler tombol "Neues Konto" di FXML
    // Tujuan:
    // - minta input nama account
    // - validasi (gak boleh kosong)
    // - createAccount di bank
    // - refresh list biar muncul
    // =========================================================
    @FXML
    private void onNewAccount() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Neues Konto");
        dialog.setHeaderText(null);
        dialog.setContentText("Accountname:");

        // showAndWait -> nunggu user input.
        // ifPresent -> jalan kalau user klik OK.
        dialog.showAndWait().ifPresent(name -> {
            String n = name.trim();

            // validasi input kosong
            if (n.isEmpty()) {
                showError("Name darf nicht leer sein.");
                return;
            }

            try {
                // bikin account + (biasanya) bikin file persistence
                bank.createAccount(n);

                // update list UI
                refreshAccounts();

            } catch (AccountAlreadyExistsException e) {
                showError("Account existiert bereits: " + n);

            } catch (IOException e) {
                showError("Fehler beim Speichern: " + e.getMessage());
            }
        });
    }

    // =========================================================
    // showError = helper buat nampilin error popup
    // Dipakai supaya UI gak crash dan user tau kenapa gagal
    // =========================================================
    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // =========================================================
    // showInfo = helper popup info (optional)
    // Dipakai kalau mau ngasih pesan sukses misalnya
    // =========================================================
    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
