#include "pcap.h"
#include "method.hpp"
#include <stdlib.h>
#include <iostream>
#include <memory.h>
using namespace std;

int main(int argc, char *argv[])
{
  pcap_header pcap_head_buf;   
  pkt_header pkt_head_buf;     
  ip_header ip_head_buf;       
  tcp_header tcp_head_buf;     
   
  FILE *fp ;                   
  FILE *tcplogtxt;             
    
  if (NULL == (fp = fopen(argv[1], "rb"))) {
    cout << "Cannot open pcap file: " << argv[0] << endl;
  }
  if (NULL == (tcplogtxt = fopen(argv[2] , "w+"))) {
    cout << "Cannot open file: "<< argv[1] << endl; 
  }
  
  getPcapFileHead(fp, pcap_head_buf);
  fseek(fp, 0, SEEK_END);
  long fileSize = ftell(fp);
  long fpOffset = sizeof(pcap_header) ;

  cout << "File size: " << fileSize << endl;
     
  while ((fseek(fp, fpOffset, SEEK_SET) == 0) && (fpOffset < fileSize)) {   //在循环中处理每一个网络帧
    getPktHead(fp, pkt_head_buf);
    fpOffset += sizeof(pkt_header) + pkt_head_buf.capture_len;

    u_int16 framType = getFramType(fp, pcap_head_buf.linktype);         //framType 标识了该帧是否为 IPV6链接
    if (framType == 0xdd86) { continue; }

    double this_time = pkt_head_buf.ts.timestamp_s + 1.0 * pkt_head_buf.ts.timestamp_ns / 1000000;
    getIpHead(fp, ip_head_buf);
    if (ip_head_buf.Protocol != 0x06 ) { continue; }

    u_int32 SrcIP; 
    u_int32 DstIP;
    u_int8 Ver_HLen;  
    u_int16 TotalLen;  
    DstIP = ip_head_buf.DstIP;
    SrcIP = ip_head_buf.SrcIP;
    Ver_HLen = ip_head_buf.Ver_HLen;
    int ip_head_len = Ver_HLen & 0x0F;
    TotalLen = ip_head_buf.TotalLen;
    unsigned int tlen = ((TotalLen & 0x00FF) << 8) | ((TotalLen & 0xFF00) >> 8);  
            
    getTcpHead(fp ,tcp_head_buf) ;
                
    double time_gap;

    u_int16 SPort = tcp_head_buf.SrcPort;     
    u_int16 DPort = tcp_head_buf.DstPort;    
    u_int32 Seq = tcp_head_buf.SeqNO; 
    u_int32 Ack = tcp_head_buf.AckNO; 
    u_int8 TCPheadLen = tcp_head_buf.HeaderLen;
    u_int8 Flag = tcp_head_buf.Flags; 
    u_int16 Win = tcp_head_buf.Window;
                        
    Win = ((Win & 0xFF00) >> 8) | ((Win & 0x00FF) << 8); 
    Seq = wrapUnsignedInt(tcp_head_buf.SeqNO); 
    Ack = wrapUnsignedInt(tcp_head_buf.AckNO); 
                     
    int port1 = ((SPort & 0xFF00) >> 8) | ((SPort & 0x00FF) << 8);
    int port2 = ((DPort & 0xFF00) >> 8) | ((DPort & 0x00FF) << 8);
    int optionLen = tcp_head_buf.HeaderLen >> 2;
                    
    int datalen = tlen - ip_head_len * 4 - optionLen;
    u_int8 kind,length;
    u_int8 mp_length = 0;
    u_int8 subtype1 = 0;
    u_int8  mp_kind = 0;
    int subtype = 0;
    u_int32 mp_dataack = 0;
    u_int32 mp_dataseq = 0;
    u_int32 mp_subseq = 0;
    u_int16 mp_datalength = 0;                        
    int sacklength = 0;
    u_int32 tsv, tser;
    u_int32 tsvsack[12];
    memset(tsvsack, 0, sizeof(tsvsack));
    /*
    for(int k = 0; k < 12; ) {
      tsvsack[k] = 0; 
      k++;
    }
    */
    //fprintf(tcplogtxt,"%d",sacka[1]);
    for(int j = 20; j < optionLen; ) {
      fread(&kind, sizeof(u_int8), 1, fp);

      if (kind == 0x00 || kind == 0x01) {
	j++;
      } else if (kind == 0x08) {
	fread(&length, sizeof(u_int8), 1, fp);
	fread(&tsv, sizeof(u_int32), 1, fp);
	fread(&tser, sizeof(u_int32), 1, fp);
	tsv = wrapUnsignedInt(tsv); 
	tser= wrapUnsignedInt(tser); 
	j += length;
      } else if (kind == 0x05) {
	fread(&length, sizeof(u_int8), 1, fp);
	sacklength = (length - 2) >> 2;
	for(int k = 0; k < sacklength; ) {
	  fread(&tsvsack[k], sizeof(u_int32), 1, fp);
	  tsvsack[k] = wrapUnsignedInt(tsvsack[k]); 
	  k++; 
	}
	j += length;
	//fprintf(tcplogtxt,"%d",sacka[5]);
      } else if (kind == 0x1e) {
	mp_kind = 30;
	
	fread(&mp_length, sizeof(u_int8), 1, fp);
	fread(&subtype1, sizeof(u_int8), 1, fp);
	subtype = (subtype1 & 0xF0) >> 4;
	fseek(fp, 1, SEEK_CUR);

	if (subtype == 2) {
	  if(mp_length == 20) {
	    // fprintf(tcplogtxt,"data ");                                  
	    fread(&mp_dataack, sizeof(u_int32), 1, fp);
	    mp_dataack = wrapUnsignedInt(mp_dataack);
	    fread(&mp_dataseq, sizeof(u_int32), 1, fp);
	    mp_dataseq = wrapUnsignedInt(mp_dataseq);
	    fread(&mp_subseq, sizeof(u_int32), 1, fp);
	    mp_subseq = wrapUnsignedInt(mp_subseq);
	    fread(&mp_datalength, sizeof(u_int16), 1, fp);
	    mp_datalength = ((mp_datalength & 0x00FF) << 8) | ((mp_datalength & 0xFF00) >> 8); 
	    fseek(fp, 2, SEEK_CUR);
	  } else if (mp_length == 8) {
	    // fprintf(tcplogtxt,"ack ");                                  
	    fread(&mp_dataack, sizeof(u_int32), 1, fp);
	    mp_dataack = wrapUnsignedInt(mp_dataack);
	  }
	} else {
	  fseek(fp, mp_length - 4, SEEK_CUR);
	}
	
	j += mp_length;
      } else {
	fread(&length, sizeof(u_int8), 1, fp);
	fseek(fp, length - 2, SEEK_CUR);
	j += length;
      }
    }

    //    cout << mp_kind << endl;
    //    if ((port1 == 80 || port2 == 80) && mp_kind == 30) {
    fprintf(tcplogtxt, " %.8f %d %d %d %d %d %d %d %d %d %d %d %d %d %d %d %d %d %d %d %d %d %d %d %d %d %d \n",
	    this_time, port1, port2, datalen, Seq,
	    Ack, Flag, Win, tsv, tser,
	    tsvsack[0], tsvsack[1], tsvsack[2],tsvsack[3], tsvsack[4],
	    tsvsack[5], tsvsack[6],	tsvsack[7], tsvsack[8], tsvsack[9],
	    mp_kind, mp_length, subtype, mp_dataack, mp_dataseq, mp_subseq,
	    mp_datalength);
    //    }
  }
       
  fclose(fp);
  fclose(tcplogtxt);

  return 0;
}


