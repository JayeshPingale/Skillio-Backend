package com.skillio.services;

import com.skillio.dto.*;
import java.util.List;

public interface LeadService {
	LeadResponse createLead(CreateLeadRequest request, Long loggedInUserId);

	LeadResponse updateLead(Long leadId, UpdateLeadRequest request);

	LeadResponse getLeadById(Long leadId);

	List<LeadResponse> getAllLeads();

	List<LeadResponse> getLeadsByStatus(String status);

	List<LeadResponse> getLeadsByAssignedUser(Long userId);

	void deleteLead(Long leadId);

	// Status management
	LeadResponse changeLeadStatus(LeadStatusChangeRequest request, Long userId);

	List<LeadStatusHistoryResponse> getLeadStatusHistory(Long leadId);

	// Assignment
	LeadResponse assignLead(AssignLeadRequest request);

	LeadResponse unassignLead(Long leadId);

	List<LeadResponse> getNonConvertedLeads();
}
