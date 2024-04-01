package com.server;

import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Util {
    static Logger logger = LogManager.getLogger(Util.class.getName());
    public static int lostReplyCount = 0;
    public static int replyDelaySec = 0;

    /**
     * Construct message response for successful operation
     */
    public static Map<String,Object> successPacket(String msg){
        Map<String,Object> successPacket = new HashMap<>();
        successPacket.put("status", Integer.valueOf(1));
        successPacket.put("message", msg);
        return successPacket;
    }

    //////////////////////////////////////////////////////////////
//Construct reply messages for failed operation
    public static String inconsistentReqCodeMsg(String req,int code){
        return "Internal main.java.com.server.Server error: the code for "
                + req + " request shall NOT be " + code + ".";
    }

    public static String inconsistentFieldTypeMsg(String field,String type){
        return "Field " + field + " shall be a type of " + type + ".";
    }

    public static String missingFieldMsg(List<String> fields){
        StringBuffer sb = new StringBuffer("Missing field ");
        for(String f:fields){
            sb.append(f + ",");
        }
        return sb.toString();
    }

    public static String failUnMarshalMsg(byte[] data){
        return "Fail to Unmarshal 0x" + DatatypeConverter.printHexBinary(data);
    }


    public static String invalidPathMsg(String path) {
        // TODO Auto-generated method stub
        return "Invalid Path " + path;
    }

    public static String nonExistFileMsg(String file) {
        return "File on path " + file + " does not exist";
    }
//////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////
    /**
     * Send the reply to the ip and port address
     */
    public static boolean sendPacket(InetAddress address,int port, Map<String,Object>response){
        try(DatagramSocket dgs = new DatagramSocket()){
            byte[] data = Util.marshal(response);
            DatagramPacket request =
                    new DatagramPacket(data, data.length, address, port);

            if(lostReplyCount > 0){
                lostReplyCount--;
                logger.info("(Lost)Reply to " + address.toString() + " at port " + port + " contents: " + response);
            }else{
                Thread.sleep(replyDelaySec * 1000);
                dgs.send(request);
                logger.info("Reply to " + address.toString() + " at port " + port + " contents: " + response);
            }

        } catch (SocketException e) {
            logger.error(e.getMessage());
            return false;
        } catch (Exception e) {
            logger.fatal(e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Construct the reply for failed operations with messages
     * @param msg
     * @return
     */
    public static Map<String,Object> errorPacket(String msg){
        Map<String,Object> errorMsg = new HashMap<>();
        errorMsg.put("status", Integer.valueOf(0));
        errorMsg.put("message", msg);
        return errorMsg;
    }

    /**
     * Perform the marshalling
     * @param parameters the reply to be sent
     * @return
     * @throws Exception
     */
    public static byte[] marshal (Map<String, Object> parameters) throws Exception{
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );

        for(String key:parameters.keySet()){
            Object value = parameters.get(key);

            outputStream.write('s');//write 's' to represent string
            outputStream.write(key.getBytes());
            outputStream.write(0); //write NULL to mark the end of string
            outputStream.write(':'); //write ':'

            if(value instanceof String){
                outputStream.write('s');//write 's' to represent string
                outputStream.write(((String)value).getBytes());
                outputStream.write(0); //write NULL to mark the end of string
            }else if(value instanceof Integer){
                Integer i = (Integer)value;
                outputStream.write('i');//write 'i' to represent string
                outputStream.write(ByteBuffer.allocate(4).putInt(i).array());
            }else if(value instanceof Long){
                Long l = (Long)value;
                outputStream.write('l');//write 'i' to represent string
                outputStream.write(ByteBuffer.allocate(8).putLong(l).array());
            }else{
                throw new Exception("Error. Can not marshal types other than string, integer and long");
            }

            outputStream.write(',');
        }

        int parity = 0;

        //Generate the parity bit so that xor all the bytes equal to 0
        for(byte b:outputStream.toByteArray()){
            parity =  parity ^ b;
        }
        outputStream.write(parity);
        return outputStream.toByteArray();
    }

    /**
     * Unmarmal the byte array to key-value mapping pairs
     * @param data
     * @return
     * @throws Exception
     */
    public static Map<String,Object> unmarshal(byte[] data) throws Exception{
        int q = 0;
        for(byte b:data){
            q = q ^ b;
        }
        if(q != 0) 	throw new Exception("The data is corrupted.");

        Map<String,Object> result = new HashMap<>();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream( );

        // Set to true if parsing the key, false if parsing the value
        boolean isParsingKey = true;
        String key = null;
        Object value = null;

        final int INT = 1;
        final int LONG = 2;
        final int STRING = 3;

        int parsingType = 0;

        int i = 0;
        while (i < data.length - 1){ //Skip the last parity bit

            if(data[i] == 's'){
                //Parsing the string
                i++; //skip the 's'
                parsingType = STRING;
                while(data[i] != 0){
                    buffer.write(data[i]);
                    i++;
                }
                i++;  //skip the empty byte
            }else if(data[i] == 'i'){
                i++; //skip the 'i'
                parsingType = INT;
                for(int c = 0;c < 4;c++){
                    buffer.write(data[i]);
                    i++;
                }
            }else if(data[i] == 'l'){
                i++; //skip the 'l'
                parsingType = LONG;

                for(int c = 0;c < 8;c++){
                    buffer.write(data[i]);
                    i++;
                }
            }else{
                throw new Exception("Unrecognized data type during parsing");
            }


            if(isParsingKey){
                //The bytes in the buffer is for key
                if(parsingType != STRING)
                    throw new Exception("The key of request must be of string type.");
                key = new String(buffer.toByteArray());
                if(data[i] != ':')
                    throw new Exception("Expect a ':' after the key");
                i++; //skip ':'
                isParsingKey = false;

            }else{
                //The bytes in the buffer is for value
                if(parsingType == STRING){
                    value = new String(buffer.toByteArray());
                }else if(parsingType == INT){
                    value = new Integer(	ByteBuffer.wrap(buffer.toByteArray()).getInt());

                }else if(parsingType == LONG){
                    value = new Long(ByteBuffer.wrap(buffer.toByteArray()).getLong());
                }else{
                    throw new Exception("Unrecognized data type");
                }

                result.put(key, value);

                if(data[i] != ',')
                    throw new Exception("Expect a ',' after the value");
                i++; //skip ','
                isParsingKey = true; //switch to parse the key
                //value

            }//End of if parsing key
            buffer.reset();
        }//End of while

        return result;
    }

}