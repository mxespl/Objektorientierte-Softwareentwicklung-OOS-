package ui;

import bank.PrivateBank;
import bank.Transaction;
import bank.Payment;
import bank.IncomingTransfer;
import bank.OutgoingTransfer;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class AccountviewController {

    @FXML private Label lblAccountName;          // label buat nampilin nama account yang dipilih
    @FXML private Label lblBalance;              // label buat nampilin saldo terbaru
    @FXML private ListView<Transaction> lvTransactions; // list transaksi (object Transaction, bukan String)

    private PrivateBank bank;    // "mesin data": create/delete/add/remove, baca file, hitung balance
    private String account;      // nama account yang lagi dibuka di view ini

    // Mode = state tampilan list (biar setelah add/delete tampilannya gak balik ke default)
    private enum Mode { ALL, ASC, DESC, POS, NEG }
    private Mode mode = Mode.ALL; // default: tampilkan semua transaksi

    // =========================================================
    // setBankAndAccount = "INIT" controller setelah scene dibuka
    // Dipanggil dari MainviewController saat pindah ke Accountview
    // =========================================================
    public void setBankAndAccount(PrivateBank bank, String account) {
        this.bank = bank;                 // simpan instance bank yang sama dari Mainview
        this.account = account;           // simpan nama account yang dipilih

        lblAccountName.setText(account);  // update UI: label nama account di atas

        setupTransactionContextMenu();    // pasang cellFactory + right-click menu delete
        refresh();                        // muat list transaksi + hitung ulang balance
    }

    // =========================================================
    // refresh = "SATU PINTU" untuk update tampilan:
    // - ambil transaksi sesuai mode (all/sort/filter)
    // - taruh ke ListView
    // - updateBalance()
    // Dipanggil setelah add/delete transaksi, dan setelah ganti mode
    // =========================================================
    private void refresh() {
        if (bank == null || account == null) return; // safety: jangan jalan kalau belum di-inject

        try {
            List<Transaction> data;

            // pilih sumber list sesuai mode user
            switch (mode) {
                case ASC:
                    data = bank.getTransactionsSorted(account, true);   // sort naik
                    break;
                case DESC:
                    data = bank.getTransactionsSorted(account, false);  // sort turun
                    break;
                case POS:
                    data = bank.getTransactionsByType(account, true);   // cuma yang amount positif
                    break;
                case NEG:
                    data = bank.getTransactionsByType(account, false);  // cuma yang amount negatif
                    break;
                case ALL:
                default:
                    data = bank.getTransactions(account);               // semua transaksi
                    break;
            }

            // masukin list ke ListView (harus ObservableList)
            lvTransactions.setItems(FXCollections.observableArrayList(data));

            updateBalance(); // setelah list update, saldo juga dihitung ulang

        } catch (Exception e) {
            showError(e.getMessage()); // error apapun tampil alert (biar app gak crash)
        }
    }

    // =========================================================
    // updateBalance = hitung saldo terbaru dari bank dan tampilkan
    // Dipanggil dari refresh(), jadi selalu update setelah perubahan
    // =========================================================
    private void updateBalance() {
        if (bank == null || account == null) return;
        try {
            double balance = bank.getAccountBalance(account); // ambil saldo dari "mesin bank"
            lblBalance.setText(String.format("%.2f €", balance)); // tampil 2 decimal
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    // =========================================================
    // onBack = handler tombol Back di FXML
    // Balik ke Mainview pada stage yang sama (NO new Stage)
    // =========================================================
    @FXML
    private void onBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/mainview.fxml"));
            Parent root = loader.load(); // baca FXML mainview -> bikin UI + controller

            MainviewController controller = loader.getController();
            controller.setBank(bank); // inject bank yg sama biar data konsisten

            Stage stage = (Stage) lvTransactions.getScene().getWindow(); // ambil window yang sedang dipakai
            stage.setScene(new Scene(root)); // ganti isi window jadi Mainview
            stage.setTitle("Bank - Mainview");
        } catch (IOException e) {
            showError("Fehler beim Laden der Mainview: " + e.getMessage());
        }
    }

    // =========================================================
    // onSortAsc = handler tombol "Sort ↑"
    // Cuma ubah mode -> refresh()
    // =========================================================
    @FXML
    private void onSortAsc() {
        mode = Mode.ASC;
        refresh();
    }

    // =========================================================
    // onSortDesc = handler tombol "Sort ↓"
    // =========================================================
    @FXML
    private void onSortDesc() {
        mode = Mode.DESC;
        refresh();
    }

    // =========================================================
    // onOnlyPositive = handler tombol "+ only"
    // =========================================================
    @FXML
    private void onOnlyPositive() {
        mode = Mode.POS;
        refresh();
    }

    // =========================================================
    // onOnlyNegative = handler tombol "- only"
    // =========================================================
    @FXML
    private void onOnlyNegative() {
        mode = Mode.NEG;
        refresh();
    }

    // =========================================================
    // setupTransactionContextMenu = bikin tampilan item ListView + menu klik kanan
    // - cellFactory dipakai supaya tiap item tampil pakai toString()
    // - dan tiap item punya context menu "Löschen"
    // =========================================================
    private void setupTransactionContextMenu() {
        lvTransactions.setCellFactory((ListView<Transaction> listView) -> {

            // cell = 1 baris item dalam ListView
            ListCell<Transaction> cell = new ListCell<Transaction>() {
                @Override
                protected void updateItem(Transaction item, boolean empty) {
                    super.updateItem(item, empty);
                    // kalau kosong -> null, kalau ada item -> tampilkan toString()
                    setText(empty || item == null ? null : item.toString());
                }
            };

            // menu item delete
            MenuItem miDelete = new MenuItem("Löschen");
            // kalau diklik -> hapus transaksi milik cell ini
            miDelete.setOnAction(e -> onDeleteTransaction(cell.getItem()));

            ContextMenu cm = new ContextMenu(miDelete);

            // kalau cell kosong, jangan kasih context menu (biar gak delete "null")
            cell.emptyProperty().addListener((obs, wasEmpty, isEmpty) ->
                    cell.setContextMenu(isEmpty ? null : cm)
            );

            return cell;
        });
    }

    // =========================================================
    // onDeleteTransaction = hapus 1 transaksi:
    // - cek null
    // - konfirmasi yes/no
    // - remove dari bank (persist)
    // - refresh list + balance (mode tetap)
    // =========================================================
    private void onDeleteTransaction(Transaction t) {
        if (t == null) {
            showError("Keine Transaktion ausgewählt.");
            return;
        }

        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setHeaderText(null);
        confirm.setContentText("Transaktion wirklich löschen?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try {
            bank.removeTransaction(account, t); // beneran hapus dari data + file (tergantung implementasi bank)
            refresh(); // update UI + balance, tetap di mode yang user pilih
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    // =========================================================
    // onNewTransaction = tombol "Neue Transaktion"
    // - user pilih type Payment/Transfer
    // - input field wajib: date, amount, desc
    // - validasi amount > 0
    // - kalau Transfer: input sender+recipient, lalu decide incoming/outgoing otomatis
    // - addTransaction -> refresh
    // =========================================================
    @FXML
    private void onNewTransaction() {
        if (bank == null || account == null) return;

        // 1) pilih jenis transaksi
        ChoiceDialog<String> typeDialog = new ChoiceDialog<String>("Payment", "Payment", "Transfer");
        typeDialog.setTitle("Neue Transaktion");
        typeDialog.setHeaderText(null);
        typeDialog.setContentText("Typ wählen:");
        String type = typeDialog.showAndWait().orElse(null);
        if (type == null) return;

        // 2) input field umum
        String date = askNonEmptyText("Datum", "Datum der Transaktion:");
        if (date == null) return;

        Double amount = askDouble("Betrag", "Betrag (z.B. 100.5):");
        if (amount == null) return;

        // VALIDASI penting: amount tidak boleh <= 0
        if (amount <= 0) {
            showError("Betrag muss > 0 sein.");
            return;
        }

        String desc = askNonEmptyText("Beschreibung", "Beschreibung:");
        if (desc == null) return;

        try {
            Transaction t;

            if ("Payment".equals(type)) {
                // buat Payment (sesuai constructor lu sekarang)
                t = new Payment(date, amount, desc);

            } else {
                // buat Transfer: butuh sender & recipient
                String sender = askNonEmptyText("Sender", "Sender (Accountname):");
                if (sender == null) return;

                String recipient = askNonEmptyText("Empfänger", "Empfänger (Accountname):");
                if (recipient == null) return;

                // RULE penting: program decide incoming/outgoing
                if (account.equals(sender) && !account.equals(recipient)) {
                    // account ini ngirim uang -> outgoing
                    t = new OutgoingTransfer(date, amount, desc, sender, recipient);

                } else if (!account.equals(sender) && account.equals(recipient)) {
                    // account ini nerima uang -> incoming
                    t = new IncomingTransfer(date, amount, desc, sender, recipient);

                } else {
                    // kalau dua-duanya bukan account atau dua-duanya account -> invalid
                    showError("Für dieses Konto muss es entweder Sender ODER Empfänger sein.");
                    return;
                }
            }

            // simpan ke bank + persistence
            bank.addTransaction(account, t);

            // update UI + balance sesuai mode
            refresh();

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    // =========================================================
    // showError = helper untuk tampilkan error popup
    // =========================================================
    private void showError(String msg) {
        Alert a = new Alert(AlertType.ERROR);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    // =========================================================
    // askNonEmptyText = helper input text:
    // - buka TextInputDialog
    // - trim
    // - tolak kosong
    // return null kalau user cancel
    // =========================================================
    private String askNonEmptyText(String title, String prompt) {
        TextInputDialog d = new TextInputDialog();
        d.setTitle(title);
        d.setHeaderText(null);
        d.setContentText(prompt);

        String v = d.showAndWait().orElse(null);
        if (v == null) return null;

        v = v.trim();
        if (v.isEmpty()) {
            showError(title + " darf nicht leer sein.");
            return null;
        }
        return v;
    }

    // =========================================================
    // askDouble = helper input angka double:
    // - panggil askNonEmptyText
    // - parseDouble
    // - error kalau bukan angka
    // =========================================================
    private Double askDouble(String title, String prompt) {
        String s = askNonEmptyText(title, prompt);
        if (s == null) return null;

        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            showError(title + " ist keine gültige Zahl.");
            return null;
        }
    }
}
