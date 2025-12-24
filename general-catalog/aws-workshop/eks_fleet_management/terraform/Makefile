.PHONY: help init plan apply destroy clean

help:
	@echo "Available targets:"
	@echo "  init    - Initialize Terraform"
	@echo "  plan    - Run terraform plan"
	@echo "  apply   - Run terraform apply"
	@echo "  destroy - Run terraform destroy"
	@echo "  clean   - Clean Terraform files"

init:
	cd environments/dev && terraform init

plan:
	cd environments/dev && terraform plan

apply:
	cd environments/dev && terraform apply

destroy:
	cd environments/dev && terraform destroy

clean:
	find . -type d -name ".terraform" -exec rm -rf {} +
	find . -type f -name "*.tfstate*" -delete
	find . -type f -name ".terraform.lock.hcl" -delete
