import org.restcomm.media.spi.memory.Frame;
import org.restcomm.media.spi.memory.Memory;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Created by luxuedong
 * 2019/7/2 10:57 AM
 */
public class G729Coder {

    public static byte[] g7292pcmJ(org.restcomm.media.codec.g729.Decoder decoder, byte[] data) {

        ByteArrayOutputStream dstBuffer = new ByteArrayOutputStream();
        try {
            Frame buffer = Memory.allocate(20);
            byte[] src = buffer.getData();
            int readLen = 0;
            while (readLen < data.length) {
                int remainLen = data.length - readLen;
                int onceLen = 20 < remainLen ? 20 : remainLen;
                System.arraycopy(data, readLen, src, 0, onceLen);
                readLen += onceLen;
                buffer.setLength(onceLen);
                byte[] encodeBytes = decoder.process(buffer).getData();
                dstBuffer.write(encodeBytes);
            }
        } catch (Exception e) {

        }
        return dstBuffer.toByteArray();
    }

    public static byte[] pcm2g729J(org.restcomm.media.codec.g729.Encoder encoder,byte[] data) {
        ByteArrayOutputStream dstBuffer = new ByteArrayOutputStream();
        try {
            Frame buffer = Memory.allocate(320);
            byte[] src = buffer.getData();
            int readLen = 0;
            while (readLen < data.length) {
                int remainLen = data.length - readLen;
                int onceLen = 320 < remainLen ? 320 : remainLen;
                System.arraycopy(data, readLen, src, 0, onceLen);
                readLen += onceLen;
                buffer.setLength(onceLen);
                byte[] encodeBytes = encoder.process(buffer).getData();
                dstBuffer.write(encodeBytes);
            }
        } catch (Exception e) {

        }
        return dstBuffer.toByteArray();
    }



//    public byte[] encodePcm2G729(byte[] src){
//        byte[] result = new byte[src.length / 16];
//
//        for(int i = 0;i<src.length/160;i++){
//            byte[] temp = new byte[160];
//            System.arraycopy(src, i*160, temp, 0, 160);
//            byte[] process = encoder.process(temp);//10
//            System.arraycopy(process, 0,result , i*10, 10);
//        }
//        return result;
//    }
//
//    public static byte[] decodeByte(byte[] bytes) {
//        G729ADecoder decoder = new G729ADecoder();
//        byte[] bb = new byte[bytes.length * 16];
//        ArrayList<Byte> list = new ArrayList<>();
//        for (int i = 0; i < bytes.length / 10; i++) {
//            byte[] b = {bytes[i * 10], bytes[1 + i * 10], bytes[2 + i * 10], bytes[3 + i * 10], bytes[4 + i * 10], bytes[5 + i * 10], bytes[6 + i * 10], bytes[7 + i * 10], bytes[8 + i * 10], bytes[9 + i * 10]};
//            byte[] process = decoder.process(b);
//            ArrayList<Byte> arr = new ArrayList<>();
//            for (int j = 0; j < process.length; j++) {
//                arr.add(process[j]);
//            }
//            list.addAll(arr);
//        }
//        for (int i = 0; i < list.size(); i++) {
//            bb[i] = list.get(i);
//        }
//        return bb;
//    }

    public static void main(String[] args) throws IOException {

        byte[] result = encodePcm();

        byte[] decode = decodeG729(result);

        byte[] bytes = concatAudio2wav(decode);
        outFile(bytes);

    }

    private static byte[] encodePcm() throws IOException {
        FileInputStream fis = new FileInputStream("/Users/luxuedong/Desktop/source.wav");

        byte[] wavByte = InputStreamToByte(fis);
        //wav to pcm
        byte[] pcm = Arrays.copyOfRange(wavByte, 44, wavByte.length);

        System.out.println("PCM audio len: " + pcm.length);
        org.restcomm.media.codec.g729.Encoder encoder = new org.restcomm.media.codec.g729.Encoder();
        byte[] result = pcm2g729J(encoder,pcm);

        System.out.println("G729 encode len []: " + result.length);
        return result;
    }

    public static void outFile(byte[] bytes) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream("/Users/luxuedong/Desktop/target.wav");
        fileOutputStream.write(bytes);
        fileOutputStream.close();
    }

    public static byte[] decodeG729(byte[] g729audio) {

        System.out.println("G729 decode to pcm len []: " + g729audio.length);
        org.restcomm.media.codec.g729.Decoder decoder = new org.restcomm.media.codec.g729.Decoder();
        byte[] result = g7292pcmJ(decoder,g729audio);

        System.out.println("G729 decode to pcm len []: " + result.length);
        return result;

    }

    private static short[] bytesToShort(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        short[] shorts = new short[bytes.length / 2];
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
        return shorts;
    }

    private static byte[] shortToBytes(short[] shorts) {
        if (shorts == null) {
            return null;
        }
        byte[] bytes = new byte[shorts.length * 2];
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(shorts);

        return bytes;
    }

    private static byte[] charsToBytes (char[] chars) {
        Charset cs = Charset.forName ("UTF-8");
        CharBuffer cb = CharBuffer.allocate (chars.length);
        cb.put (chars);
        cb.flip ();
        ByteBuffer bb = cs.encode (cb);

        return bb.array();

    }

    private static char[] bytesToChars (byte[] bytes) {
        Charset cs = Charset.forName ("UTF-8");
        ByteBuffer bb = ByteBuffer.allocate (bytes.length);
        bb.put (bytes);
        bb.flip ();
        CharBuffer cb = cs.decode (bb);

        return cb.array();
    }


    private static byte[] InputStreamToByte(FileInputStream fis) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        long size = fis.getChannel().size();
        byte[] buffer = null;
        if (size <= Integer.MAX_VALUE) {
            buffer = new byte[(int) size];
        } else {
            buffer = new byte[8];
            for (int ix = 0; ix < 8; ++ix) {
                int offset = 64 - (ix + 1) * 8;
                buffer[ix] = (byte) ((size >> offset) & 0xff);
            }
        }
        int len;
        while ((len = fis.read(buffer)) != -1) {
            byteStream.write(buffer, 0, len);
        }
        byte[] data = byteStream.toByteArray();
        byteStream.close();
        return data;
    }

    private static byte[] concatAudio2wav(byte[] bodybyte) {
        try {
            byte[] headerbyte = getWaveHeader(bodybyte);
            byte[] wavbytes = concatByte(headerbyte, bodybyte);

            return wavbytes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] concatByte(byte[] b1, byte[] b2) {
        byte[] concatByte = new byte[b1.length + b2.length];
        for (int i = 0; i < concatByte.length; i++) {
            if (i < b1.length) {
                concatByte[i] = b1[i];
            } else {
                concatByte[i] = b2[i - b1.length];
            }
        }
        return concatByte;
    }

    public static byte[] getWaveHeader(byte[] pcm) throws Exception {
        int PCMSize = pcm.length;
        WaveHeader header = new WaveHeader();
        header.fileLength = PCMSize + (44 - 8);
        header.FmtHdrLeth = 16;
        header.BitsPerSample = 16;
        header.Channels = 1;
        header.FormatTag = 0x0001;
        header.SamplesPerSec = 8000;
        header.BlockAlign = (short) (header.Channels * header.BitsPerSample / 8);
        header.AvgBytesPerSec = header.BlockAlign * header.SamplesPerSec;
        header.DataHdrLeth = PCMSize;
        byte[] h = header.getHeader();
        return h;
    }

    static class WaveHeader {
        public final char fileID[] = {'R', 'I', 'F', 'F'};
        public int fileLength;
        public char wavTag[] = {'W', 'A', 'V', 'E'};
        public char FmtHdrID[] = {'f', 'm', 't', ' '};
        public int FmtHdrLeth;
        public short FormatTag;
        public short Channels;
        public int SamplesPerSec;
        public int AvgBytesPerSec;
        public short BlockAlign;
        public short BitsPerSample;
        public char DataHdrID[] = {'d', 'a', 't', 'a'};
        public int DataHdrLeth;

        public byte[] getHeader() throws IOException {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            WriteChar(bos, fileID);
            WriteInt(bos, fileLength);
            WriteChar(bos, wavTag);
            WriteChar(bos, FmtHdrID);
            WriteInt(bos, FmtHdrLeth);
            WriteShort(bos, FormatTag);
            WriteShort(bos, Channels);
            WriteInt(bos, SamplesPerSec);
            WriteInt(bos, AvgBytesPerSec);
            WriteShort(bos, BlockAlign);
            WriteShort(bos, BitsPerSample);
            WriteChar(bos, DataHdrID);
            WriteInt(bos, DataHdrLeth);
            bos.flush();
            byte[] r = bos.toByteArray();
            bos.close();
            return r;
        }

        private void WriteShort(ByteArrayOutputStream bos, int s)
                throws IOException {
            byte[] mybyte = new byte[2];
            mybyte[1] = (byte) ((s << 16) >> 24);
            mybyte[0] = (byte) ((s << 24) >> 24);
            bos.write(mybyte);
        }

        private void WriteInt(ByteArrayOutputStream bos, int n) throws IOException {
            byte[] buf = new byte[4];
            buf[3] = (byte) (n >> 24);
            buf[2] = (byte) ((n << 8) >> 24);
            buf[1] = (byte) ((n << 16) >> 24);
            buf[0] = (byte) ((n << 24) >> 24);
            bos.write(buf);
        }

        private void WriteChar(ByteArrayOutputStream bos, char[] id) {
            for (int i = 0; i < id.length; i++) {
                char c = id[i];
                bos.write(c);
            }
        }
    }
}
