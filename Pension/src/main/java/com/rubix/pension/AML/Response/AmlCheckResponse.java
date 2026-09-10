package com.rubix.pension.AML.Response;

import java.util.List;

public class AmlCheckResponse {
 private String status;
 private int totalMatches;
 private List<AmlMatchResult> matches;
 private String message;

 public String getMessage() {
  return message;
 }

 public void setMessage(String message) {
  this.message = message;
 }

 public String getStatus() {
  return status;
 }
 public void setStatus(String status) {
  this.status = status;
 }
 public int getTotalMatches() {
  return totalMatches;
 }
 public void setTotalMatches(int totalMatches) {
  this.totalMatches = totalMatches;
 }
 public List<AmlMatchResult> getMatches() {
  return matches;
 }
 public void setMatches(List<AmlMatchResult> matches) {
  this.matches = matches;
 }
 
}
