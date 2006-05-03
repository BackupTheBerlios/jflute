
package mcl;

public class MCL_JNI
{

    /* various defines */
    final public static int DEL_MODE_PUSH          =0;      /* tx once, assumes synchronized rx */
    final public static int DEL_MODE_ON_DEMAND     =1;      /* tx cyclically, rx start at any time*/
    final public static int DEL_MODE_STREAMING     =2;      /* streaming mode */
    final public static int MCL_TX_PROFILE_LOW_RATE_INTERNET  =0;   /* modem to=2Mbps connections */
    final public static int MCL_TX_PROFILE_MID_RATE_INTERNET  =1;   /* eg with VPNs, xDSL */
    final public static int MCL_TX_PROFILE_HIGH_SPEED_INTERNET=2;   /* eg for fast/Gbps ethernet */
    final public static int MCL_TX_PROFILE_HIGH_SPEED_LAN     =3;   /* eg for fast/Gbps ethernet */
    final public static int MCL_SCHED_LCT1         =0;      /* see mcl_profile.h for descr. */
    final public static int MCL_SCHED_LCT2         =1;      /* see mcl_profile.h for descr. */
    final public static int MCL_SCHED_LCT3         =2;      /* see mcl_profile.h for descr. */
    final public static int MCL_SCHED_NB           =3;      /* nb of schedulers defined */
    final public static int MCL_SCHED_SEQUENTIAL_OBJ_ORDER =0;/* tx ADUs in sequence */
    final public static int MCL_SCHED_RANDOM_OBJ_ORDER     =1;/* tx ADUs in random order in each layer */
    final public static int MCL_SCHED_PARTIALLY_MIXED_ORDER=2;/* mix partially all DUs of all ADUs*/
    final public static int MCL_SCHED_MIXED_ORDER          =3;/* mix all DUs of all ADUs */
    final public static int MCL_FEC_CODE_NULL      =0;      /* null code (i.e. no FEC encoding) */
    final public static int MCL_FEC_CODE_RSE       =1;      /* Reed-Solomon erasure FEC code */
    final public static int MCL_FEC_CODE_LDGM      =2;      /* Low Density Generator Matrix */
    final public static int MCL_FEC_CODE_LDPC      =3;      /* Low Density Parity Check */
    final public static int MCL_FEC_CODE_MAX_NB    =4;      /* Max number of FEC codes available */
    
    
    static{System.loadLibrary("mcl_jni");}

        public native int mcl_open(String mode);
        public native int mcl_close(int id);
        public native int mcl_abort(int id);
        public native int mcl_wait_event (int id, int event);
        public native int mcl_send (int id, byte [] data, int len);
        public native int mcl_recv (int id, byte [] data, int len);
        public native int mcl_recv_flute (int id, byte [] data, int len, int [] toi);
        
        /* various functions of mcl_ctl split in separate routines */
        /* Descriptions of the functionality of these functions can be
           found in mcl_lib_api_alc.h of the mcl implementation of ALC */
        public native int mcl_ctl_VERBOSITY (int id, int verbosity);
        public native int mcl_ctl_STATS (int id, int stats);
        public native int mcl_ctl_PORT (int id, int port);
        public native int mcl_ctl_ADDR (int id, String addr);
        public native int mcl_ctl_TTL (int id, int ttl);
        public native int mcl_ctl_LAYER (int id, int layer);
        public native int mcl_ctl_LAYER_NB (int id, int layer);
        public native int mcl_ctl_MOREABOUT (int id);
        public native int mcl_ctl_DEBUG (int id);
        public native int mcl_ctl_SINGLE_LAYER (int id, int layer);
        public native int mcl_ctl_DEMUX_LABEL (int id, int label);
        public native int mcl_ctl_TMP_DIR (int id, String tmpdir);               
        public native int mcl_ctl_DELIVERY_MODE (int id, int mode);
        public native int mcl_ctl_NETIF (int id, int netif);
        public native int mcl_ctl_LOOPBACK (int id, int loopback);
        public native int mcl_ctl_SET_NEXT_TOI (int id, int toi);
        public native int mcl_ctl_TX_PROFILE(int id, int profile);
        public native int mcl_ctl_DATAGRAM_SIZE(int id, int size);
        public native int mcl_ctl_TX_RATE(int id, int rate);
        public native int mcl_ctl_FEC_RATIO(int id, float ratio);
        public native int mcl_ctl_NB_OF_TX(int id, int nb);
        public native int mcl_ctl_REUSE_APPLI_TX_BUFFER(int id, int reuse);
        public native int mcl_ctl_VIRTUAL_TX_MEMORY(int id, int mem);
        public native int mcl_ctl_VIRTUAL_RX_MEMORY(int id, int mem);
        public native int mcl_ctl_KEEP_DATA(int id);
        public native int mcl_ctl_PUSH_DATA(int id);
        public native int mcl_ctl_RESET_TRANSMISSIONS(int id );
        public native int mcl_ctl_SCHED(int id, int sched );
        public native int mcl_ctl_OBJ_SCHED(int id, int sched);
        public native int mcl_ctl_SET_FEC_CODE(int id, int code);
        public native int mcl_ctl_GET_MAX_BLOCK_SIZE_FOR_CURRENT_FEC(int id, int max);
        public native int mcl_ctl_SRC_ADDR(int id, int addr);
        public native int mcl_ctl_IMMEDIATE_DELIVERY(int id, int del);
        public native int mcl_ctl_POSTPONE_FEC_DECODING(int id, int post );
        public native int mcl_ctl_NEVER_LEAVE_BASE_LAYER(int id, int never);
        public native int mcl_ctl_SP_CYCLE(int id, int cycle);
        public native int mcl_ctl_PKT_TIMEOUT(int id, int timeout);
        public native int mcl_ctl_DEAF_PERIOD(int id, int deaf);
        public native int mcl_ctl_LATE_ACCEPTED(int id, int late);
        public native int mcl_ctl_LOSS_ACCEPTED(int id, int loss);
        public native int mcl_ctl_LOSS_LIMIT(int id, int limit);
        public native int mcl_ctl_LOSS_TIMEOUT(int id, int limit);
        public native int mcl_ctl_AGGRESSIVE_CC(int id, int agressive_cc);
        public native int mcl_ctl_FLIDS_TSD(int id, int tsd);
        public native int mcl_ctl_FLIDS_DEAF_PERIOD (int id, int period);
        public native int mcl_ctl_FLUTE_DELIVERY(int id);
        public native int mcl_ctl_FLUTE_DELIVER_THIS_ADU(int id, int adu);
        public native int mcl_ctl_FLUTE_DELIVER_ALL_ADU(int id);
        public native int mcl_ctl_NO_NONEWADU(int id);
/*        
Interface done: extern int mcl_open (const char *mode);
Interface done: extern int mcl_close (int id);
Interface done: extern int mcl_abort (int id);
Interface done: extern int mcl_ctl  (int id, int optname, void *optvalue, int optlen);
Interface done: extern int mcl_send (int id, const void *data, int len);
Interface done: extern int mcl_recv (int id, void *buf, int len);
Interface done: extern int mcl_wait_event (int id, int event);

Not done (since it's tricky to convert 'sockaddr' and 'fd_set' between Java and C
and also because we don't really need any of these for flute:
  extern int mcl_recvfrom (int id, void * buf, int len, struct sockaddr *saddr, int *saddr_len);
  extern int mcl_sendto (int id, const void *data, int len, const struct sockaddr *saddr, int saddr_len);
  extern int mcl_select (int n, fd_set *readfds, fd_set *writefds, fd_set *exceptfds, struct timeval *timeout);        
*/
}
