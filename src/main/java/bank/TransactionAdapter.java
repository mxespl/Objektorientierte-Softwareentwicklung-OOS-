package bank;

import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * Custom Gson serializer and deserializer for the abstract type {@link Transaction}.
 * <p>
 * It wraps the concrete object into a JSON object with the fields:
 * <ul>
 *     <li>{@code CLASSNAME} – simple class name of the concrete transaction
 *     (e.g. {@code "Payment"}, {@code "IncomingTransfer"}, {@code "OutgoingTransfer"})</li>
 *     <li>{@code INSTANCE} – the serialized fields of the concrete object</li>
 * </ul>
 * This allows Gson to reconstruct the correct subclass during deserialization.
 */
public class TransactionAdapter implements JsonSerializer<Transaction>, JsonDeserializer<Transaction> {

    /**
     * Serializes the given {@link Transaction} into a JSON object with fields
     * {@code CLASSNAME} and {@code INSTANCE}.
     *
     * @param src        the transaction to serialize
     * @param typeOfSrc  the actual type (ignored here, we use {@link Class#getSimpleName()})
     * @param context    the serialization context
     * @return a JSON representation of the transaction
     */
    @Override
    public JsonElement serialize(Transaction src, Type typeOfSrc, JsonSerializationContext context) {
        //bikin object Json kosong to be filled
        JsonObject root = new JsonObject();
        // ikutin format sekarang kita add property to the object yaitu CLASSNAME
        root.addProperty("CLASSNAME", src.getClass().getSimpleName());
        //abis classname udah sekarang kita mau bikin instancenya
        //// 3) serialize objek src biasa jadi JsonElement (INSTANCE)

        JsonElement instance = context.serialize(src);// instance itu objectjson
        //masukin ke object root lagi
        root.add("INSTANCE", instance); //add itu add(key, Jsonobject) dan casenya ini instance.
        return root; //balikkan object root
    }

    /**
     * Deserializes a JSON object with fields {@code CLASSNAME} and {@code INSTANCE}
     * back into the appropriate {@link Transaction} subclass.
     *
     * @param json      the JSON element to deserialize
     * @param typeOfT   the target type (ignored, always {@link Transaction})
     * @param context   the deserialization context
     * @return the deserialized transaction
     * @throws JsonParseException if the {@code CLASSNAME} is unknown
     */
    @Override
    public Transaction deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        //1. dapetin rootnya dulu sebagai objek
        JsonObject root = json.getAsJsonObject();
        //2.
        String className = root.get("CLASSNAME").getAsString();
        JsonElement instance = root.get("INSTANCE");

        Class<? extends Transaction> clazz; // clazz boleh berisi class apa pun yang merupakan turunan Transaction.
        switch (className) { //classname disini tuh masih string yang baru diubah dari json
            case "Payment" -> clazz = Payment.class;
            case "IncomingTransfer" -> clazz = IncomingTransfer.class;
            case "OutgoingTransfer" -> clazz = OutgoingTransfer.class;
            default -> throw new JsonParseException("Unknown CLASSNAME: " + className); // buat handle typo
        }

        return context.deserialize(instance, clazz); // bikin objeknya dengan instance yang ada dan tipe class yang ditemui
    }
}
