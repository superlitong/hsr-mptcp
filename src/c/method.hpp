#ifndef METHOD_H
#define METHOD_H

#include "pcap.h"
#include <cstdio>
#include <iostream>
#include <vector>

void getPcapFileHead(FILE *fp, pcap_header &pcap_head)
{
  fread(&pcap_head, sizeof(pcap_header), 1, fp);
}


void getPktHead(FILE *fp, pkt_header &pkt_head)
{
  fread(&pkt_head, sizeof(pkt_header), 1, fp);
}

u_int32 getFramType(FILE *fp , u_int32 linktype)
{
  if (linktype == 0x71) {
    Linux_cooked_capture temp;
    fread(&temp ,sizeof(temp) ,1 , fp);
    return temp.FrameType;
  }
  if (linktype == 0x01) {
    Ethernet temp;
    fread(&temp ,sizeof(temp) ,1 , fp);
    return temp.FrameType ;
  }
}

void getIpHead(FILE *fp, ip_header &ip_head_buf)
{
  fread(&ip_head_buf, sizeof(ip_header), 1, fp);
}
 
void getTcpHead(FILE *fp, tcp_header &tcp_head_buf)
{
  fread(&tcp_head_buf, sizeof(tcp_header), 1, fp);
}
void getUdpHead(FILE *fp, udp_header &udp_head_buf)
{
  fread(&udp_head_buf, sizeof(udp_header), 1, fp);
}
void getUdpDataHead(FILE *fp , udp_data_header &udp_data_head_buf)
{
  fread(&udp_data_head_buf, sizeof(udp_data_header), 1, fp);
}

void getTcpHeadAndSkipOption(FILE *fp , tcp_header &tcp_head_buf)
{
  fread(&tcp_head_buf, sizeof(tcp_header), 1, fp);
  fseek(fp, (tcp_head_buf.HeaderLen >> 2) - sizeof(tcp_header), SEEK_CUR);
}

void getTcpOption12(FILE *fp , option12 &option12_buf)
{
  fread(&option12_buf, sizeof(option12), 1, fp);
}

void getTcpOption20(FILE *fp , option20 &option20_buf)
{
  fread(&option20_buf, sizeof(option20), 1, fp);
}

void getTcpOption24(FILE *fp , option24 &option24_buf)
{
  fread(&option24_buf, sizeof(option24), 1, fp);
}

unsigned int wrapUnsignedInt(unsigned int n) {
  /*
  unsigned int b = ((n & 0x00FFFF00) << 8) | ((n & 0x00FFFF00) >> 8);
  unsigned int c = b & 0x00FFFF00;
  unsigned int d = n >> 24 | n << 24;
  n = c | d; 
  return n;
  */
  return ((n & 0x000000ff) << 24) | ((n & 0x0000ff00) << 8)
    | ((n & 0x00ff0000) >> 8) | ((n & 0xff000000) >> 24);
}

void matchHttp(char tcp_data_buf[], std::string &methodBuf, std::string &urlBuf, std::string &hostBuf, std::string &uaBuf)
{
  std::vector<std::string> tempStrVector;
  std::string tempSring(tcp_data_buf);

  for(std::string::size_type beganPos = 0; beganPos != tempSring.size(); ) {  //将tcp_data_buf[] 内的字符串
    std::string::size_type endPos=beganPos;                                 //按照 " \n " 分组放入tempStrVecor
    while(++endPos && endPos != tempSring.size()) {
      if( tempSring[endPos] =='\n' ) {
	break;
      }
    }
    tempStrVector.push_back(tempSring.substr(beganPos, endPos - beganPos));
    if (endPos == tempSring.size()) { break; }
    beganPos = endPos ;
  }

  for(std::vector<std::string>::iterator posVector = tempStrVector.begin();
      posVector != tempStrVector.end();
      ++posVector) {
    if (std::string::size_type tempPos = (*posVector).find("GET") != (*posVector).npos) {
      methodBuf = "GET";
      std::string::size_type endPos = (*posVector).find("HTTP/1.1");
      urlBuf = (*posVector).substr(tempPos + sizeof("GET") - 1, endPos - tempPos - sizeof("GET"));
    }
    if (std::string::size_type tempPos = (*posVector).find("POST") != (*posVector).npos) {
      std::string::size_type endPos = (*posVector).find("HTTP/1.1");
      methodBuf = "POST";
      urlBuf = (*posVector).substr(tempPos+sizeof("POST") -1, endPos - tempPos - sizeof("POST") );
    }
    if (std::string::size_type tempPos = (*posVector).find("Host:") != (*posVector).npos) {
      hostBuf=(*posVector).substr(tempPos+sizeof("Host:"));
    } 
    if ( std::string::size_type tempPos = (*posVector).find("User-Agent:") != (*posVector).npos) {
      uaBuf=(*posVector).substr(tempPos+sizeof("User-Agent:")   );
    }
  }
}
#endif // METHOD_H
